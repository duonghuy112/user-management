package nguyenduonghuy.usermanagement.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MokitokSpyTest {
	@Spy
	List<String> spiedList = new ArrayList<>();
	 
	@Test
	public void whenUseSpyAnnotation_thenSpyIsInjected() {
	    spiedList.add("one");
	    spiedList.add("two");
	 
	    verify(spiedList).add("one");
	    verify(spiedList).add("two");
	    assertThat(spiedList.get(0)).isEqualTo("one");
	    assertThat(spiedList.size()).isEqualTo(2);
	    
	    Mockito.doReturn(100).when(spiedList).size();
	    assertThat(spiedList.size()).isEqualTo(100);
	}
}
