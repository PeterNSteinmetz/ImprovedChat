import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Standard version of LineFilter, which checks for rejections then accept rules.
 * 
 * @author peter
 */
public class DefaultLineFilter implements LineFilter {
  
	private ArrayList<Pattern> trackPatterns = new ArrayList<Pattern>();
	private ArrayList<Pattern> ignorePatterns = new ArrayList<Pattern>();
	
	@Override
	public boolean isValid(String line) {
	    if (line != null && !line.trim().equals("")) {
	        Iterator<Pattern> patIter = ignorePatterns.iterator();
	
	        Pattern p;
	        while (patIter.hasNext()) {
	            p = patIter.next();
	            if (p.matcher(line).find()) {
	                return false;
	            }
	        }
	
	        patIter = trackPatterns.iterator();
	
	        while (patIter.hasNext()) {
	            p = patIter.next();
	            if (p.matcher(line).find()) {
	                return true;
	            }
	        }
	
	        return false;
	    } else {
	        return false;
	    }
	}
	
	@Override
	public boolean isIgnored(String line) {
	    Iterator<Pattern> patIter = this.ignorePatterns.iterator();
	
	    while (patIter.hasNext()) {
	        Pattern p = patIter.next();
	        if (p.matcher(line).find()) {
	            return true;
	        }
	    }
	
	    return false;
	}
	@Override
	public void addTrackPattern(String regex) {
		try {
			Pattern pat = Pattern.compile(regex);
			trackPatterns.add(pat);
		} catch (PatternSyntaxException pse) {
			;	// at least prevent crash
		}		
	}
	
	@Override
	public void addIgnorePattern(String regex) {
		try {
			Pattern pat = Pattern.compile(regex);
			ignorePatterns.add(pat);
		} catch (PatternSyntaxException pse) {
			;	// at least prevent crash
		}		
	}

	@Override
	public List<Pattern> getTrackPatterns() {
		return trackPatterns;
	}

	@Override
	public void deleteTrackPattern(int patInd) {
		if (patInd>0 && patInd<trackPatterns.size()) trackPatterns.remove(patInd);
	}

	@Override
	public List<Pattern> getIgnorePatterns() {
		return ignorePatterns;
	}

	@Override
	public void deleteIgnorePattern(int patInd) {
		if (patInd>0 && patInd<ignorePatterns.size()) ignorePatterns.remove(patInd);
	}

}
