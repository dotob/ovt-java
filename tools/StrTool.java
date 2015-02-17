package tools;

public class StrTool {

	public static String strHead(String input, int to){
		String ret = "";
		int l = input.length();
		if (to>0 && to<l){
			to  = Math.min(l, to);
			ret = input.substring(0, to);
		} else if (to<0 && to*-1<l){
			to  = Math.max(l*-1, to);
			ret = input.substring(0, l+to);
		}
		return ret;
	}

	public static String strTail(String input, int to){
		String ret = "";
		int l = input.length();
		if (to>0){
			to  = Math.min(l, to);
			ret = input.substring(l-to, l);
		} else if (to<0){
			to  = Math.max(l*-1, to);
			ret = input.substring(to*-1, l);
		}
		return ret;
	}

	public static String strToken(String input, int which, String trenner){
		String ret = "";
		int i = 1;
		int von = 0;
		while (which>1 && i<which && von>=0){
			von = input.indexOf(trenner, von+1)+1;
			i++;
		}
		if (von<0){
			return "";
		}
		int bis = input.indexOf(trenner, von+1);
		if (bis<0){
			bis = input.length();
		} 
		ret = input.substring(von, bis);
		return ret;
	}
	
	public static String fillChar(String in, String fill, int step){
		String out = "";
		for(int i=0; i<in.length();i++){
			if (i>0 && i%step==0){
				out += fill;
			}
			out += in.charAt(i);
		}
		return out;
	}
}
