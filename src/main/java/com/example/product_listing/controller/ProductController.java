package com.example.product_listing.controller;

import com.example.product_listing.model.Product;
import com.example.product_listing.model.ProductDto;
import com.example.product_listing.service.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository prepo;

@GetMapping({"","/"})
    public  String showProductList(Model model){
        List<Product> products = prepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }
@GetMapping("/create")
    public  String  showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result){
    if(productDto.getImageFile().isEmpty()){
        result.addError(new FieldError("productDto" , "imageFile","The image file is requried"));
    }
    if(result.hasErrors()){
        return "products/CreateProduct";
    }

    // save image in databases

        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_"+ image.getOriginalFilename();
        try {
            String uploadDir ="public/phone_img/";
            Path uplaodPath = Paths.get(uploadDir);

            if (!Files.exists(uplaodPath)){
                Files.createDirectories(uplaodPath);
            }
            try (InputStream inputStream=image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);


            }
        }catch (Exception ex){
            System.out.println("Exception:" + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        prepo.save(product);




    return  "redirect:/products";
    }

    @GetMapping("/edit")
    public  String showEditPage(
            Model model, @RequestParam int id
    ){

    try {
        Product product = prepo.findById(id).get();
        model.addAttribute("product", product);


        ProductDto productDto = new ProductDto();
        productDto.setName(product.getName());
        productDto.setBrand(product.getBrand());
        productDto.setCategory(product.getCategory());
        productDto.setPrice(product.getPrice());
        productDto.setDescription(product.getDescription());

        model.addAttribute("productDto", productDto);





    }catch (Exception ex){
        System.out.println("Exception"+ ex.getMessage());
        return "redirect:/products";
    }
    return "products/EditProduct";
    }


    @PostMapping("/edit")
    public  String updateProduuct( Model model
      , @RequestParam int id,
                                   @Valid @ModelAttribute ProductDto productDto,
                                   BindingResult result){

    try {
        Product product = prepo.findById(id).get();
        model.addAttribute("product"+product);

        if(result.hasErrors()){
            return "products/EditProduct";
        }

        if (!productDto.getImageFile().isEmpty()){
            String upladDir = "public/phone_img/";
            Path oldImagePath = Paths.get(upladDir + product.getImageFileName());

            try {
                Files.delete(oldImagePath);

            }catch (Exception ex){
                System.out.println("Exception"+ ex.getMessage());
            }

            // save image file in databases
            MultipartFile image = productDto.getImageFile();
            Date createdAt = new Date();
            String storageFileName = createdAt.getTime() + "__"+ image.getOriginalFilename();
            try(InputStream inputStream = image.getInputStream())
            {
                Files.copy(inputStream, Paths.get(upladDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            product.setImageFileName(storageFileName);

        }

        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());

prepo.save(product);

    }catch (Exception ex){
        System.out.println("Exception"+ ex.getMessage());
    }

    return "redirect:/products";
    }


    @GetMapping("/delete")
    public String deleteProduct(
            @RequestParam int id
    ){
    try {
        Product product = prepo.findById(id).get();


        //Delete product from databaes;

        Path imagePath = Paths.get("public/phone_img/" + product.getImageFileName());
        try {
            Files.delete(imagePath);
        }catch (Exception ex){
            System.out.println("Exception"+ ex.getMessage());
        }
        prepo.delete(product);

    }catch (Exception ex){
        System.out.println("Exception"+ ex.getMessage());
    }
    return "redirect:/products";
    }

}
