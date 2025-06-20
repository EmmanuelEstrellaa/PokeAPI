package com.digis01.PokeAPI.Controller;

import com.digis01.PokeAPI.ML.PokeTipo1;
import com.digis01.PokeAPI.ML.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/Pokemones")
public class PokeController {

    
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public String Index(Model model) {
        String url = "https://pokeapi.co/api/v2/type";

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Map>() {
        }
        );

        List<Map<String, String>> tipos = (List<Map<String, String>>) response.getBody().get("results");

        model.addAttribute("tipos", tipos);
        return "Index";
    }

    @GetMapping("/tipo/{id}")
    public String PokeTipo1(@PathVariable int id, @RequestParam(defaultValue = "1")int page, Model model) {
        Result result = new Result();
        String url = "https://pokeapi.co/api/v2/type/" + id;
        int pageSize = 12;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Map>() {
        }
        );

        List<Map<String, Object>> pokemones = (List<Map<String, Object>>) response.getBody().get("pokemon");

        List<PokeTipo1> detalles = new ArrayList<>();

        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, pokemones.size());
        
        for (int i = start; i < end; i++) {
            Map<String, Object> poke = pokemones.get(i);
            Map<String, String> details = (Map<String, String>) poke.get("pokemon");
            String nombre = details.get("name");

            String PokeUrl = "https://pokeapi.co/api/v2/pokemon/" + nombre;

            try {
                ResponseEntity<Map> responsePokemon = restTemplate.exchange(
                        PokeUrl,
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Map>() {
                });

                Map<String, Object> datos = responsePokemon.getBody();

                //Sprite
                Map<String, String> sprites = (Map<String, String>) datos.get("sprites");
                //Stats
                List<Map<String, Object>> stats = (List<Map<String, Object>>) datos.get("stats");

                PokeTipo1 saveDetails = new PokeTipo1();

                saveDetails.setNombre(nombre);
                saveDetails.setImagen((String) sprites.get("front_default"));
                saveDetails.setHp((Integer) stats.get(0).get("base_stat"));
                saveDetails.setAtaque((Integer) stats.get(1).get("base_stat"));
                saveDetails.setDefensa((Integer) stats.get(2).get("base_stat"));

                detalles.add(saveDetails);

            } catch (Exception ex) {
                result.correct = false;
                result.ex = ex;
                result.errorMessage = ex.getLocalizedMessage();

            }

        }
        
        int totalPages = (int) Math.ceil((double) pokemones.size() / pageSize);

        model.addAttribute("pokemones", detalles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("tipoId", id);
        return "PokeTipo1";
    }

    @GetMapping("/PokeDetails")
    public String PokeDetails(){
        return "PokeDetails";
    }
    
   
    
}
