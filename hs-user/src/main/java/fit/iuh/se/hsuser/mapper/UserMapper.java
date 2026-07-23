package fit.iuh.se.hsuser.mapper;

import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "displayName", source = "profile.displayName")
    @Mapping(target = "phone", source = "profile.phone")
    @Mapping(target = "dateOfBirth", source = "profile.dateOfBirth")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "avatarUrl", source = "profile.avatarUrl")
    @Mapping(target = "address", source = "profile.address")
    UserResponse toUserResponse(UserAccount user);
}
