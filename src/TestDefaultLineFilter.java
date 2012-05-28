


import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class TestDefaultLineFilter {

	DefaultLineFilter filt;
	
	@Before
	public void setUp() throws Exception {
		filt = new DefaultLineFilter();
	}
	
	// test ignore rules
	@Test
	public void testIgnoreRules() {
		filt.addIgnorePattern("ABC");
		assertEquals(1,filt.getIgnorePatterns().size());
		assertEquals(0,filt.getTrackPatterns().size());
		assertTrue(filt.isIgnored("Now is the time ABC for all good"));
		assertFalse(filt.isIgnored("Now is the time for all good"));
		assertFalse(filt.isValid("Now is the time ABC for all good"));
	}
	
	// test track rules
	@Test
	public void testTrackRules() {
		filt.addTrackPattern("DEF");
		assertEquals(0,filt.getIgnorePatterns().size());
		assertEquals(1,filt.getTrackPatterns().size());
		assertTrue(filt.isValid("Now is the time DEF for all good"));
		assertFalse(filt.isIgnored("Now is the time DEF for all good"));
		assertFalse(filt.isValid("Now is the time ABC for all good"));
	}
	
	// test both types adding
	@Test
	public void testBothRulesAdding() {
		filt.addIgnorePattern("ABC");
		filt.addTrackPattern("DEF");
		assertEquals(1,filt.getIgnorePatterns().size());
		assertEquals(1,filt.getTrackPatterns().size());
		assertFalse(filt.isValid("Now is the time for all good."));
		assertTrue(filt.isValid("Now is the time DEF for all good"));
		assertFalse(filt.isValid("Now is the time ABC for all good"));
		assertFalse(filt.isValid("Now is the time ABCDEF for all good"));
	}
	
	// test overlapping rules
	@Test
	public void testOverlappingRules() {
		filt.addIgnorePattern("ABC");
		filt.addTrackPattern("ABC");
		assertEquals(1,filt.getIgnorePatterns().size());
		assertEquals(1,filt.getTrackPatterns().size());
		assertFalse(filt.isValid("Now is the time for all good."));
		assertTrue(filt.isIgnored("Now is the time ABC for all good."));
		assertFalse(filt.isValid("Now is the time ABC for all good"));
	}
	
	// test reverse overlapping order
	@Test
	public void testReversedRules() {
		filt.addTrackPattern("ABC");
		filt.addIgnorePattern("ABC");
		assertEquals(1,filt.getIgnorePatterns().size());
		assertEquals(1,filt.getTrackPatterns().size());
		assertFalse(filt.isValid("Now is the time for all good."));
		assertTrue(filt.isIgnored("Now is the time ABC for all good."));
		assertFalse(filt.isValid("Now is the time ABC for all good"));
	}
	
	
}
