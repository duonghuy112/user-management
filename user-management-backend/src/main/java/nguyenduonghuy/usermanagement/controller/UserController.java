package nguyenduonghuy.usermanagement.controller;

import static nguyenduonghuy.usermanagement.constant.FileConstant.FORWARD_SLASH;
import static nguyenduonghuy.usermanagement.constant.FileConstant.TEMP_PROFILE_IMAGE_BASE_URL;
import static nguyenduonghuy.usermanagement.constant.FileConstant.USER_FOLDER;
import static nguyenduonghuy.usermanagement.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import nguyenduonghuy.usermanagement.controller.cms.GlobalExceptionHandle;
import nguyenduonghuy.usermanagement.domain.User;
import nguyenduonghuy.usermanagement.domain.UserPrincipal;
import nguyenduonghuy.usermanagement.exception.EmailExistException;
import nguyenduonghuy.usermanagement.exception.EmailNotFoundException;
import nguyenduonghuy.usermanagement.exception.NotAnImageFileException;
import nguyenduonghuy.usermanagement.exception.UserNotFoundException;
import nguyenduonghuy.usermanagement.exception.UsernameExistException;
import nguyenduonghuy.usermanagement.exception.response.HttpResponse;
import nguyenduonghuy.usermanagement.service.UserService;
import nguyenduonghuy.usermanagement.utils.JwtTokenProvider;

@RestController
@RequestMapping("api/users")
public class UserController extends GlobalExceptionHandle {
	private static final String EMAIL_SENT = "An email with a new password was sent to: ";
    private static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
	
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAll();
        return new ResponseEntity<>(users, OK);
    }
	
	@GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        User user = userService.findByUsername(username);
        return new ResponseEntity<>(user, OK);
    }
	
    @GetMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
        userService.resetPassword(email);
        HttpResponse response = new HttpResponse(OK.value(), OK, OK.getReasonPhrase(), EMAIL_SENT + email);
        return new ResponseEntity<>(response, OK);
    }
    
    @GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getAvatar(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempAvatar(@PathVariable("username") String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
		User newUser = userService.register(user.getFullname(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
		User loginUser = userService.findByUsername(user.getUsername());
		UserPrincipal userLoginPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeaders = new HttpHeaders();
		jwtHeaders.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userLoginPrincipal));
		return new ResponseEntity<>(loginUser, jwtHeaders, OK);
	}
	
	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestBody User user,
			@RequestParam(value = "avatar", required = false) MultipartFile avatar) 
					throws UserNotFoundException, UsernameExistException, EmailExistException, NotAnImageFileException, IOException, MessagingException {
		User newUser = userService.addNew(user.getFullname(), user.getUsername(), user.getEmail(), user.getRole(), user.isNotLocked(), user.isActive(), avatar);
		return new ResponseEntity<>(newUser, HttpStatus.CREATED);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<User> updateUser(@PathVariable("id") String id, 
			@RequestBody User user,
			@RequestParam(value = "avatar", required = false) MultipartFile avatar) 
					throws NumberFormatException, UserNotFoundException, UsernameExistException, EmailExistException, NotAnImageFileException, IOException {
		User updateUser = userService.update(Long.parseLong(id), user.getFullname(), user.getUsername(), user.getEmail(), user.getRole(), user.isNotLocked(), user.isActive(), avatar);
		return new ResponseEntity<>(updateUser, HttpStatus.CREATED);
	}
	
	@PutMapping("/updateAvatar/{id}")
    public ResponseEntity<User> updateAvatar(@PathVariable("id") String id, @RequestParam(value = "avatar") MultipartFile avatar) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User user = userService.updateAvatar(Long.parseLong(id), avatar);
        return new ResponseEntity<>(user, OK);
    }
    
    @DeleteMapping("/delete/{id}")
//    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") String id) throws IOException {
        userService.delete(Long.parseLong(id));
        HttpResponse response = new HttpResponse(OK.value(), OK, OK.getReasonPhrase(), USER_DELETED_SUCCESSFULLY);
        return new ResponseEntity<>(response, OK);
    }
}
