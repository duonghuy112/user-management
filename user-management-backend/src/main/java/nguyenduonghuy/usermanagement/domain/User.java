package nguyenduonghuy.usermanagement.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	private Long id;
	private String userId;
	@NotBlank
	private String fullname;
	@NotBlank
	private String username;
	private String password;
	@Email
	private String email;
	private String avatar;
	private LocalDateTime lastLoginDateDisplay;
	private LocalDateTime lastLoginDate;
	@CreationTimestamp
	private LocalDateTime joinDate;
	private String role;
	private String[] authorities;
	private boolean isActive;
	private boolean isNotLocked;
	
	public User(String fullname, String username, String email) {
		this.fullname = fullname;
		this.username = username;
		this.email = email;
	}
}
