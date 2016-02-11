package util;

public class RegExpTest {
	
	public static void main(String[] args) {
		String re = "[a-zA-Z]\\.?";
		String test = "S";
		System.out.println(test.matches(re));
	}
	
}
