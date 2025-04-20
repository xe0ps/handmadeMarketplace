package shepelev.handmadeMarketplace.service;

import shepelev.handmadeMarketplace.entity.Image;
import shepelev.handmadeMarketplace.repo.ImageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageService {
    @Autowired
    private ImageRepo imageRepo;

    public Image saveImage(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setFileName(file.getOriginalFilename());
        image.setFileType(file.getContentType());
        image.setData(file.getBytes());
        return imageRepo.save(image);
    }
}