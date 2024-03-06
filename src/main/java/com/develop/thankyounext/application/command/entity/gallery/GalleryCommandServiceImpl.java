package com.develop.thankyounext.application.command.entity.gallery;

import com.develop.thankyounext.domain.dto.base.common.AuthenticationDto;
import com.develop.thankyounext.domain.dto.gallery.GalleryRequest.RegisterGallery;
import com.develop.thankyounext.domain.dto.result.ResultResponse.GalleryResult;
import com.develop.thankyounext.domain.entity.Gallery;
import com.develop.thankyounext.domain.entity.Image;
import com.develop.thankyounext.domain.repository.gallery.GalleryRepository;
import com.develop.thankyounext.domain.repository.image.ImageRepository;
import com.develop.thankyounext.global.exception.handler.GalleryHandler;
import com.develop.thankyounext.global.manager.amazon.s3.AmazonS3Manger;
import com.develop.thankyounext.global.payload.code.status.ErrorStatus;
import com.develop.thankyounext.infrastructure.config.aws.AmazonConfig;
import com.develop.thankyounext.infrastructure.converter.GalleryConverter;
import com.develop.thankyounext.infrastructure.converter.ImageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.develop.thankyounext.domain.dto.gallery.GalleryRequest.UpdateGallery;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GalleryCommandServiceImpl implements GalleryCommandService {
    private final ImageRepository imageRepository;

    private final GalleryRepository galleryRepository;

    private final GalleryConverter galleryConverter;
    private final ImageConverter imageConverter;

    // Amazon S3 Link
    private final AmazonS3Manger amazonS3Manger;
    private final AmazonConfig amazonConfig;

    @Override
    public GalleryResult registerGallery(AuthenticationDto auth, RegisterGallery request, List<MultipartFile> fileList) {

        Gallery newGallery = galleryConverter.toGallery(request);

        List<Image> imageList = createImages(fileList, newGallery);

        Gallery saveGallery = galleryRepository.save(newGallery);
        imageRepository.saveAll(imageList);

        return galleryConverter.toGalleryResult(saveGallery);
    }

    @Override
    public GalleryResult updateGallery(AuthenticationDto auth, UpdateGallery request, List<MultipartFile> fileList) {

        Gallery currentGallery = galleryRepository.findById(request.galleryId())
                .orElseThrow(() -> new GalleryHandler(ErrorStatus.GALLERY_NOT_FOUND));

        if (request.title() != null && !request.title().isEmpty()) {
            currentGallery.updateTitle(request.title());
        }

        currentGallery.getImageList().getImageList().forEach(image -> deleteImageFromS3(image.getUrl()));

        imageRepository.deleteAllByGalleryId(request.galleryId());
        List<Image> imageList = createImages(fileList, currentGallery);
        currentGallery.setImageList(imageConverter.toGalleryImageList(imageList));
        imageRepository.saveAll(imageList);

        return galleryConverter.toGalleryResult(currentGallery);
    }

    private List<Image> createImages(List<MultipartFile> fileList, Gallery gallery) {
        List<Image> imageList = fileList.stream().map(file -> {
            Image image = imageConverter.toImage(file, amazonS3Manger, amazonConfig.getPostPath());
            image.setGallery(gallery);
            return image;
        }).toList();
        gallery.setImageList(imageConverter.toGalleryImageList(imageList));
        return imageList;
    }

    private void deleteImageFromS3(String url) {
        amazonS3Manger.deleteImageForS3(amazonS3Manger.extractImageKeyFromUrl(url));
    }
}
