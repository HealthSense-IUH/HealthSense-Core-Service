package fit.iuh.se.hsauth.mapper;

import fit.iuh.se.hsauth.dto.response.RegisterResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", source = "profile.displayName")
    @Mapping(target = "accountStatus", source = "status")
    RegisterResponse toUserSession(UserAccount user);
}
