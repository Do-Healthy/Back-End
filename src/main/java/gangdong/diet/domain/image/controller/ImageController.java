package gangdong.diet.domain.image.controller;

import gangdong.diet.domain.cookingstep.service.CookingStepService;
import gangdong.diet.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/images")
@RequiredArgsConstructor
@RestController
public class ImageController {

    private final ImageService imageService;

    @Operation
    @PostMapping
    public ResponseEntity<List<String>> uploadImages(@RequestPart("images") List<MultipartFile> images) {
        List<String> imageNames = imageService.uploadImages(images);
        return ResponseEntity.ok().body(imageNames);
    }

//    @Operation
//    @PutMapping
//    public ResponseEntity<List<String>> updateImages(@RequestPart("images") List<MultipartFile> images,
//                                                     @RequestParam("urlLists") List<String> urlLists)  {
//        List<String> imageNames = imageService.updateImages(images, urlLists);
//        return ResponseEntity.ok().body(imageNames);
//    }

}
