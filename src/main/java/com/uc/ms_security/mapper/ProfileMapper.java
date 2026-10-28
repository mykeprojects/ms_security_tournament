package com.uc.ms_security.mapper;

import com.uc.ms_security.dto.profile.*;
import com.uc.ms_security.entity.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProfileMapper {
    public Profile toEntity(CreateProfileDTO dto) {
        Profile profile = new Profile();
        profile.setPhone(dto.getPhone());
        profile.setPhotoUrl(dto.getPhotoUrl());
        profile.setBirthDate(dto.getBirthDate());
        return profile;
    }
    public void updateEntity(UpdateProfileDTO dto, Profile profile) {
        profile.setPhone(dto.getPhone());
        profile.setPhotoUrl(dto.getPhotoUrl());
        profile.setBirthDate(dto.getBirthDate());
    }
    public ProfileResponseDTO toResponseDTO(Profile profile) {
        if(profile == null){
            return null;
        }

        return new ProfileResponseDTO(profile.getId(), profile.getUser().getId(), profile.getPhone(), profile.getPhotoUrl(), profile.getBirthDate());
    }
    public List<ProfileResponseDTO> toResponseDTOList(List<Profile> profiles) {
        return profiles.stream().map(this::toResponseDTO).toList();
    }
}