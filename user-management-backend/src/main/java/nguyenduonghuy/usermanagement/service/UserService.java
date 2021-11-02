package nguyenduonghuy.usermanagement.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import nguyenduonghuy.usermanagement.domain.User;
import nguyenduonghuy.usermanagement.exception.EmailExistException;
import nguyenduonghuy.usermanagement.exception.EmailNotFoundException;
import nguyenduonghuy.usermanagement.exception.NotAnImageFileException;
import nguyenduonghuy.usermanagement.exception.UserNotFoundException;
import nguyenduonghuy.usermanagement.exception.UsernameExistException;

public interface UserService {
	
    List<User> getAll();

    User findByUsername(String username);

    User findByEmail(String email);
    
    User register(String fullname, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException;
    
    User addNew(String fullname, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile avatar) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException, MessagingException;

    User update(Long id, String newFullname, String newUsername, String newEmail, String newRole, boolean newIsNonLocked, boolean newIsActive, MultipartFile newAvatar) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;

    void delete(Long id) throws IOException;

    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    User updateAvatar(Long id, MultipartFile newAvatar) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;
}
