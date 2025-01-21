package gangdong.diet.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import gangdong.diet.domain.cookingstep.repository.CookingStepRepository;
import gangdong.diet.global.exception.ApiException;
import gangdong.diet.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ImageService {

    private final CookingStepRepository cookingStepRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;


//    // 단일 파일 업로드
//    public String uploadFile(MultipartFile multipartFile, String folderName) {
//        return uploadFile(List.of(multipartFile), folderName).get(0);
//    }
////    String s3name = folderName+"/"+uuid+"_"+originalFilename;
//    // 여러개의 파일 업로드
//    public List<String> uploadFile(List<MultipartFile> multipartFile, String folderName) {
//        List<String> fileNameList = new ArrayList<>();
//
//        multipartFile.forEach(file -> {
//            String fileName = createFileName(file.getOriginalFilename()); // TODO : 파일 이름에 폴더도 추가해야함. 그리고 원본 이름도 저장 할 지
//            fileName = folderName + fileName; // Todo 두 개의 폴더를 타는지 확인.
//            ObjectMetadata objectMetadata = new ObjectMetadata();
//            objectMetadata.setContentLength(file.getSize());
//            objectMetadata.setContentType(file.getContentType());
//
//            try(InputStream inputStream = file.getInputStream()) {
////                s3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
////                        .withCannedAcl(CannedAccessControlList.PublicRead));
//                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
//            } catch(IOException e) {
//                throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
//            }
//
//            fileNameList.add(fileName);
//        });
//
//        return fileNameList;
//    }

    public String uploadFile(MultipartFile file, String folderName) {
        // 파일 이름 생성
        String fileName = createFileName(file.getOriginalFilename()); // TODO : 파일 이름에 폴더도 추가해야 함. 그리고 원본 이름도 저장할지
        fileName = folderName + fileName; // TODO : 두 개의 폴더를 타는지 확인.

        // S3 업로드에 필요한 메타데이터 설정
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return fileName;
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    // 파일명 중복 방지 (UUID)
    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }


    // 파일 유효성 검사를 위한 세트
    private static final Set<String> VALID_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    // 파일 유효성 검사
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new ApiException(ErrorCode.FILE_VALID_ERROR);
        }

        // 확장자 추출
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new ApiException(ErrorCode.FILE_FORMAT_ERROR);
        }

        String extension = fileName.substring(lastDotIndex).toLowerCase();
        if (!VALID_EXTENSIONS.contains(extension)) {
            throw new ApiException(ErrorCode.FILE_FORMAT_ERROR);
        }

        return extension;
    }

    // S3에서 업로드된 파일 삭제
    public void cleanupUploadedFiles(List<String> fileNames) {
        fileNames.forEach(fileName -> {
            try {
                amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패: {}", fileName, e);
            }
        });
    }

}
