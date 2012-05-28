
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;


public class TestFixedRulesFilter {
	
	FixedRulesFilter filt;
	
	@Before
	public void setUp() throws Exception {
		filt = new FixedRulesFilter();
	}
	
	@Test
	public void testLevelUpStyle() {
		filt.setPrefixPattern("\\[P\\]");
		filt.addAllowPattern("\\[P\\] Monsters:");
		
		assertTrue(filt.isValid("[G][P] Monsters:"));
		assertTrue(filt.isValid("[G] Monsters:"));
		assertTrue(filt.isValid("[P] Monsters:"));
		assertFalse(filt.isValid("[P] tester"));
		assertTrue(filt.isValid("Monsters"));
	}
	
}
