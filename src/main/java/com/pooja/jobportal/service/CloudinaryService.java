package com.pooja.jobportal.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pooja.jobportal.dto.ResumeResponse;
import com.pooja.jobportal.model.Resume;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.ResumeRepository;
import com.pooja.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public CloudinaryService(Cloudinary cloudinary, ResumeRepository resumeRepository, UserRepository userRepository) {
        this.cloudinary = cloudinary;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public Resume uploadResume(MultipartFile file, User user) throws IOException {
        // 1. Upload to Cloudinary
        // resource_type: "auto" detects if it's image, video, or raw file (PDF/DOC)
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));

        String fileUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        
        // 2. Build the Entity
        Resume resume = new Resume();
        resume.setName(file.getOriginalFilename());
        resume.setFileUrl(fileUrl);
        resume.setCloudinaryId(publicId);
        resume.setUser(user);

        // 3. Generate Preview URL
        // If it's a PDF, Cloudinary can convert page 1 to an image by changing extension to .jpg
        if (fileUrl.endsWith(".pdf")) {
            resume.setPreviewUrl(fileUrl.replace(".pdf", ".jpg"));
        } else if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".png")) {
            resume.setPreviewUrl(fileUrl); // Images are their own preview
        } else {
            // For DOC/DOCX, Cloudinary free plan doesn't auto-generate visual previews easily.
            // We can set a default placeholder icon here.
            resume.setPreviewUrl("https://img.icons8.com/color/96/microsoft-word-2019.png");
        }

        return resumeRepository.save(resume);
    }
    
    public Resume getResume(Long id) {
        return resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("Resume not found"));
    }
    
    public Resume getResume(Long id, User user) {
        return resumeRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Resume not found or access denied"));
    }
    
    public List<ResumeResponse> getUserResumesWithPrimaryStatus(User user) {
        List<Resume> resumes = resumeRepository.findByUser(user);
        Long primaryResumeId = user.getPrimaryResume() != null ? user.getPrimaryResume().getId() : null;
        
        return resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .name(resume.getName())
                        .fileUrl(resume.getFileUrl())
                        .previewUrl(resume.getPreviewUrl())
                        .cloudinaryId(resume.getCloudinaryId())
                        .createdAt(resume.getCreatedAt())
                        .isPrimary(resume.getId().equals(primaryResumeId))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Set a resume as the primary resume for a user
     */
    public Resume setPrimaryResume(Long resumeId, User user) {
        Resume resume = resumeRepository.findByUserAndId(user, resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found or access denied"));
        
        // Update user's primary resume
        user.setPrimaryResume(resume);
        userRepository.save(user);
        
        return resume;
    }
    
    public ResumeResponse getResumeWithPrimaryStatus(Long id, User user) {
        Resume resume = resumeRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Resume not found or access denied"));
        
        boolean isPrimary = user.getPrimaryResume() != null &&
                user.getPrimaryResume().getId().equals(resume.getId());
        
        return ResumeResponse.builder()
                .id(resume.getId())
                .name(resume.getName())
                .fileUrl(resume.getFileUrl())
                .previewUrl(resume.getPreviewUrl())
                .cloudinaryId(resume.getCloudinaryId())
                .createdAt(resume.getCreatedAt())
                .isPrimary(isPrimary)
                .build();
    }
}