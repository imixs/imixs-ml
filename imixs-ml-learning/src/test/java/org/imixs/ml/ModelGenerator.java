package org.imixs.ml;

/**
 * This Java App can be used to generate a JWT Token
 * 
 * The application expects two parameters: password and payload token:
 * 
 *  <pre>
 *     java -cp classes org.imixs.ml.ModelGenerator secret {}
 *  </pre>
 *   
 * @author rsoika  
 *
 */
public class ModelGenerator {
	public static void main(String[] args)  {
		
		
		if (args==null || args.length<2){
			System.out.println("Missing parameters. Usage: java -cp classes org.imixs.jwt.TokenGenerator mypassword {\"sub\":\"admin\",\"displayname\":\"Administrator\",\"groups\":\"xxx,yyy\"}");
			return;
		}
		
		// get params
		String secret=args[0];
		String payload=args[1];

		
	
		
	}

}
