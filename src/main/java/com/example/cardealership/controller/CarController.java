package com.example.cardealership.controller;

import com.example.cardealership.model.Car;
import com.example.cardealership.model.UserDetails;
import com.example.cardealership.repository.CarRepository;
import com.example.cardealership.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
public class CarController {

    @Autowired
    CarRepository carRepo;

    @Autowired
    CategoryRepository categoryRepo;

    private static String UPLOADED_FOLDER = "./uploads/";


    @GetMapping("/cars")
    public String showCars (Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) auth.getPrincipal();
        model.addAttribute("user", user);
        model.addAttribute("car", new Car());
        model.addAttribute("cars", carRepo.findAll());
        model.addAttribute("added", false);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("activeLink", "Automobili");
        return "cars";
    }

    @GetMapping("/car/delete/{id}")
    public String deleteCar(@PathVariable("id") Long id, Model model) throws IOException {
        Car car = carRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Pogrešan ID"));
        carRepo.delete(car);
        Files.delete(Path.of(car.getImage()));
        return "redirect:/cars";
    }

    @GetMapping("/car/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) auth.getPrincipal();
        model.addAttribute("user", user);

        Car car = carRepo.findById(id).orElseThrow(() -> new IllegalArgumentException());
        model.addAttribute("car", car);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("activeLink", "Proizvodi");
        return "car_edit";
    }

    @PostMapping("/car/edit/{id}")
    public String editCar (@PathVariable("id") Long id, Car car, BindingResult result, @RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            result.addError(new FieldError("car", "image", "Molimo odaberite sliku."));
        } else if (!file.getContentType().startsWith("image/")) {
            result.addError(new FieldError("car", "image", "Molimo slika nije ispravnog formata."));
        } else {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
                Files.write(path, bytes);
                car.setImage(path.toString());
            } catch (IOException e) {
                result.addError(new FieldError("car", "image", "Problem s učitavanjem slike na poslužitelj."));
            }
        }

        if (result.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) auth.getPrincipal();
            model.addAttribute("user", user);
            model.addAttribute("car", car);
            model.addAttribute("categories", categoryRepo.findAll());
            model.addAttribute("activeLink", "Automobili");
            return "car_edit";
        }
        carRepo.save(car);
        return "redirect:/cars";
    }


    @PostMapping("/car/add")
    public String addCar (@Valid Car car, BindingResult result, @RequestParam("file") MultipartFile file, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) auth.getPrincipal();
        model.addAttribute("user", user);
        model.addAttribute("activeLink", "Automobili");

        if (file.isEmpty()) {
            result.addError(new FieldError("car", "image", "Molimo odaberite sliku."));
        } else if (!file.getContentType().startsWith("image/")) {
            result.addError(new FieldError("car", "image", "Molimo slika nije ispravnog formata."));
        } else {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
                Files.write(path, bytes);
                car.setImage(path.toString());
            } catch (IOException e) {
                result.addError(new FieldError("car", "image", "Problem s učitavanjem slike na poslužitelj."));
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("car", car);
            model.addAttribute("added", true);
            model.addAttribute("cars", carRepo.findAll());
            model.addAttribute("categories", categoryRepo.findAll());
            return "cars";
        }
        carRepo.save(car);
        return "redirect:/cars";
    }
}