package nguyenduonghuy.usermanagement.user;

import static nguyenduonghuy.usermanagement.constant.UserServiceImplConstant.EMAIL_ALREADY_EXISTS;
import static nguyenduonghuy.usermanagement.constant.UserServiceImplConstant.NO_USER_FOUND_BY_ID;
import static nguyenduonghuy.usermanagement.constant.UserServiceImplConstant.USERNAME_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import nguyenduonghuy.usermanagement.domain.User;
import nguyenduonghuy.usermanagement.exception.EmailExistException;
import nguyenduonghuy.usermanagement.exception.NotAnImageFileException;
import nguyenduonghuy.usermanagement.exception.UserNotFoundException;
import nguyenduonghuy.usermanagement.exception.UsernameExistException;
import nguyenduonghuy.usermanagement.repository.UserRepository;
import nguyenduonghuy.usermanagement.service.EmailService;
import nguyenduonghuy.usermanagement.service.LoginAttemptService;
import nguyenduonghuy.usermanagement.service.UserService;
import nguyenduonghuy.usermanagement.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private LoginAttemptService loginAttemptService;
	@Mock
	private EmailService emailService;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	
	private UserService underTest;
	
	private Long id;
	private String fullname;
	private String username;
	private String email;
	private String role;
	private boolean isNotLocked;
	private boolean isActive;
	private MultipartFile avatar;
	
	@BeforeEach
	public void setUp() {
		underTest = new UserServiceImpl(userRepository, loginAttemptService, emailService,  passwordEncoder);
		id = 1L;
		fullname = "test123";
		username = "test123";
		email = "test123@mail.com";
		role = "ROLE_USER";
		isNotLocked = true;
		isActive = true;
		avatar = null;
	}
	
	@Test
	@DisplayName("Can get all User")
	public void canGetAllUser() {
		// when
		underTest.getAll();
		// then
		verify(userRepository).findAll();
	}
	
	@Test
	@DisplayName("Can get User by username")
	public void canGetUserByUsername() {
		// when
		underTest.findByUsername(username);
		// then
		verify(userRepository).findByUsername(username);
	}
	
	@Test
	@DisplayName("Can get User by email")
	public void canGetUserByEmail() {
		// when
		underTest.findByEmail(email);
		// then
		verify(userRepository).findByEmail(email);
	}
	
	@Test
	@DisplayName("Can register User")
	public void canRegisterUser() throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
		//given
		BDDMockito.given(userRepository.findByUsername(username)).willReturn(null);
		BDDMockito.given(userRepository.findByEmail(email)).willReturn(null);
		// when
		User newUser = underTest.register(fullname, username, email);
		// then
		ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
		
		verify(userRepository).save(userArgumentCaptor.capture());
		assertThat(newUser).isEqualTo(userArgumentCaptor.getValue());
	}
	
	@Test
	@DisplayName("Cannot register User because Username existed")
	public void cannotRegisterUsername() {
		// given
		User user = new User(fullname, username, email);
		BDDMockito.given(userRepository.findByUsername(user.getUsername())).willReturn(user);
		// when & then
		assertThatThrownBy(() -> underTest.register(user.getFullname(), user.getUsername(), user.getEmail()))
			.isInstanceOf(UsernameExistException.class)
			.hasMessage(USERNAME_ALREADY_EXISTS);
		
		verify(userRepository, never()).save(ArgumentMatchers.any());
	}
	
	@Test
	@DisplayName("Cannot register User because Email existed")
	public void cannotRegisterEmail() {
		// given
		User user = new User(fullname, username, email);
		BDDMockito.given(userRepository.findByEmail(user.getEmail())).willReturn(user);
		// when & then
		assertThatThrownBy(() -> underTest.register(user.getFullname(), user.getUsername(), user.getEmail()))
			.isInstanceOf(EmailExistException.class)
			.hasMessage(EMAIL_ALREADY_EXISTS);
		
		verify(userRepository, never()).save(ArgumentMatchers.any());
	}
	
	@Test
	@DisplayName("Can add new User")
	public void canAddNewUser() throws UserNotFoundException, UsernameExistException, EmailExistException, NotAnImageFileException, IOException, MessagingException {		
		// given
		BDDMockito.given(userRepository.findByUsername(username)).willReturn(null);
		BDDMockito.given(userRepository.findByEmail(email)).willReturn(null);
		// when
		User newUser = underTest.addNew(fullname, username, email, role, isNotLocked, isActive, avatar);
		// then
		ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
		
		verify(userRepository).save(userArgumentCaptor.capture());
		assertThat(newUser).isEqualTo(userArgumentCaptor.getValue());
	}
	
	@Test
	@DisplayName("Can update User")
	@Disabled
	public void canUpdateUser() throws UserNotFoundException, UsernameExistException, EmailExistException, NotAnImageFileException, IOException, MessagingException {		
		// given
		BDDMockito.given(userRepository.existsById(id)).willReturn(true);
		// when
		User updateUser = underTest.update(id, fullname, username, email, role, isNotLocked, isActive, avatar);
		// then
		ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
		
		verify(userRepository).save(userArgumentCaptor.capture());
		assertThat(updateUser).isEqualTo(userArgumentCaptor.getValue());
	}
	
	@Test
	@DisplayName("Cannot update User")
	public void cannotUpdateUser() throws UserNotFoundException, UsernameExistException, EmailExistException, NotAnImageFileException, IOException {		
		// when & then
		assertThatThrownBy(() -> underTest.update(id, fullname, username, email, role, isNotLocked, isActive, avatar))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage(NO_USER_FOUND_BY_ID);
	
		verify(userRepository, never()).save(ArgumentMatchers.any());
	}
	
	@Test
	@DisplayName("Can delete User")
	public void canDeleteUser() throws IOException {
		// given
		BDDMockito.given(userRepository.getById(id)).willReturn(new User(fullname, username, email));
		// when
		underTest.delete(id);
		// then 
		verify(userRepository).deleteById(id);
	}
}
