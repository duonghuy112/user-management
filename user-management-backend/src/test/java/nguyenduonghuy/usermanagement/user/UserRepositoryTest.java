package nguyenduonghuy.usermanagement.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import nguyenduonghuy.usermanagement.domain.User;
import nguyenduonghuy.usermanagement.repository.UserRepository;

@DataJpaTest
public class UserRepositoryTest {

	@Autowired
	private UserRepository underTest;
	
	@BeforeEach
	public void setUp() {
		underTest.saveAll(Arrays.asList(
				new User("test01", "test01", "test01@mail.com"),
				new User("test02", "test02", "test02@mail.com"),
				new User("test03", "test03", "test03@mail.com")));
	}
	
	@AfterEach
    public void tearDown() {
        underTest.deleteAll();
    }
	
	@Test
	@DisplayName("Test find all User")
	public void testFindAll() {
		List<User> expected = underTest.findAll();
		
		System.out.println(expected);
		
		assertThat(expected).hasSize(2);
	}
	
	@Test
	@DisplayName("Test find User by username")
	public void testFindByUsername() {
		User expected = underTest.findByUsername("test01");
		
		System.out.println(expected);
		
		assertThat(expected).isNotNull();
	}
	
	@Test
	@DisplayName("Test find User by email")
	public void testFindByEmail() {
		User expected = underTest.findByEmail("test02@mail.com");
		
		System.out.println(expected);
		
		assertThat(expected).isNotNull();
	}
}
