package nguyenduonghuy.usermanagement.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static nguyenduonghuy.usermanagement.constant.FileConstant.*;
import static nguyenduonghuy.usermanagement.constant.UserServiceImplConstant.*;
import static org.springframework.http.MediaType.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;
import nguyenduonghuy.usermanagement.domain.User;
import nguyenduonghuy.usermanagement.domain.UserPrincipal;
import nguyenduonghuy.usermanagement.enumeration.Role;
import nguyenduonghuy.usermanagement.exception.EmailExistException;
import nguyenduonghuy.usermanagement.exception.EmailNotFoundException;
import nguyenduonghuy.usermanagement.exception.NotAnImageFileException;
import nguyenduonghuy.usermanagement.exception.UserNotFoundException;
import nguyenduonghuy.usermanagement.exception.UsernameExistException;
import nguyenduonghuy.usermanagement.repository.UserRepository;
import nguyenduonghuy.usermanagement.service.EmailService;
import nguyenduonghuy.usermanagement.service.LoginAttemptService;
import nguyenduonghuy.usermanagement.service.UserService;

@Service
@Transactional
@Qualifier("userDetailsService")
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

	private UserRepository userRepository;
	private LoginAttemptService loginAttemptService;
	private EmailService emailService;
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, LoginAttemptService loginAttemptService, EmailService emailService, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public List<User> getAll() {
		return userRepository.findAll();
	}

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			log.error(NO_USER_FOUND_BY_USERNAME  + username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
		} else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(LocalDateTime.now());
			userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			log.info(FOUND_USER_BY_USERNAME + username);
			return userPrincipal;
		}
	}
	
	@Override
	public User register(String fullname, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
		validateNewUsernameAndEmail(username, email);
		String password = generatePassword();
		User user = new User(null, generateUserId(), fullname, username, encodePassword(password), email, getTemporaryProfileImageUrl(username), null, null, LocalDateTime.now(), Role.ROLE_USER.name(), Role.ROLE_USER.getAuthorities(), true, true);
		userRepository.save(user);
		log.info("New user's password: " + password);
		emailService.sendNewPasswordToEmail(fullname, password, email);
		return user;
	}
	
	@Override
	public User addNew(String fullname, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile avatar) 
			throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException, MessagingException {
		validateNewUsernameAndEmail(username, email);
		String password = generatePassword();
		User user = new User(null, generateUserId(), fullname, username, encodePassword(password), email, getTemporaryProfileImageUrl(username), null, null, LocalDateTime.now(), getRoleName(role).name(), getRoleName(role).getAuthorities(), isNotLocked, isActive);
        userRepository.save(user);
        log.info("New user's password: " + password);
        saveAvatar(user, avatar);
        emailService.sendNewPasswordToEmail(fullname, password, email);
		return user;
	}

	@Override
	public User update(Long id, String newFullname, String newUsername, String newEmail, String newRole, boolean newIsNonLocked, boolean newIsActive, MultipartFile newAvatar) 
			throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
		User user = validateUpdateUsernameAndEmail(id, newUsername, newEmail);
		user.setFullname(newFullname);
		user.setUsername(newUsername);
		user.setEmail(newEmail);
		user.setRole(getRoleName(newRole).name());
		user.setAuthorities(getRoleName(newRole).getAuthorities());
		user.setNotLocked(newIsNonLocked);
		user.setActive(newIsActive);
		userRepository.save(user);
		saveAvatar(user, newAvatar);
		return user;
	}

	@Override
	public void delete(Long id) throws IOException {
		User user = userRepository.getById(id);
        Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        userRepository.deleteById(id);
	}

	@Override
	public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
		User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        log.info("New user's password: " + password);
        emailService.sendNewPasswordToEmail(user.getFullname(), password, user.getEmail());
	}

	@Override
	public User updateAvatar(Long id, MultipartFile newAvatar) 
			throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
		User user = validateUpdateUsernameAndEmail(id, StringUtils.EMPTY, StringUtils.EMPTY);
		saveAvatar(user, newAvatar);
		return user;
	}
	
	private void validateNewUsernameAndEmail(String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User userByNewUsername = findByUsername(newUsername);
        User userByNewEmail = findByEmail(newEmail);
        if(userByNewUsername != null) {
        	throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
        }
        if(userByNewEmail != null) {
        	throw new EmailExistException(EMAIL_ALREADY_EXISTS);
        }
    }
	
	private User validateUpdateUsernameAndEmail(Long id, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
		Optional<User> currentUser = userRepository.findById(id);
		User userByNewUsername = userRepository.findByUsername(newUsername);
		User userByNewEmail = userRepository.findByEmail(newEmail);
		if (!currentUser.isPresent()) {
			throw new UserNotFoundException(NO_USER_FOUND_BY_ID);
		} else {
			if (userByNewUsername != null && !id.equals(userByNewUsername.getId())) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if (userByNewEmail != null && !id.equals(userByNewEmail.getId())) {
				throw new EmailExistException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser.get();
		}
	}
	
	private void validateLoginAttempt(User user) {
        if(user.isNotLocked()) {
            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
	
	private void saveAvatar(User user, MultipartFile avatar) throws IOException, NotAnImageFileException {
        if (avatar != null) {
            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(avatar.getContentType())) {
                throw new NotAnImageFileException(avatar.getOriginalFilename() + NOT_AN_IMAGE_FILE);
            }
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                log.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(avatar.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setAvatar(setAvatarUrl(user.getUsername()));
            userRepository.save(user);
            log.info(FILE_SAVED_IN_FILE_SYSTEM + avatar.getOriginalFilename());
        }
    }
	
	private String setAvatarUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
    }
	
	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}
	
	private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
	
	private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
	
	private String getTemporaryProfileImageUrl(String username) {
//        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
		return null;
	}
	
	private Role getRoleName(String role) {
		return Role.valueOf(role.toUpperCase());
	}
}
