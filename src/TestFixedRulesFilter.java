
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;


public class TestFixedRulesFilter {

	FixedRulesFilter filt;
	
	@Before
	public void setUp() throws Exception {
		filt = new FixedRulesFilter("^ABC");
	}
	
	@Test
	public void testMatching() {
		assertTrue(filt.isValid("ABCDEF"));
		assertFalse(filt.isValid("123ABC"));
		assertFalse(filt.isValid("DEF hello"));
	}
	
}
