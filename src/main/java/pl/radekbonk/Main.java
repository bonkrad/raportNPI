package pl.radekbonk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
		System.out.println(System.getProperty("os.name"));
		//com.sun.security.auth.module.NTSystem NTSystem = new com.sun.security.auth.module.NTSystem();
		//System.out.println(NTSystem.getName());
	}

	/*@Bean
	public CommandLineRunner productDemo(ProductRepository repository) {
		return (args) -> {
			repository.save(new ProductEntity(34526,"[ST] PCBA Base board"));
			repository.save(new ProductEntity(34760,"[ST] Assembly Sculpto 3D Printer S2 001"));
			repository.save(new ProductEntity(31598,"[HU] STB 91-00637 ESD-160S"));
			repository.save(new ProductEntity(20164,"[HU] STB 91-00438 DTR-T2100/500G/BT/DF"));

		};
	}*/

	public static String getUploadPath() {
		if(System.getProperty("os.name").contains("Windows")) {
			return "H://disk/";
		} else {
			return "/home/pi/raport/disk/";
		}
	}


	//WSDL
	/*@Bean
	public CommandLineRunner lookup(ProductClient productClient) {
		return (args) -> {
			String id = "34760";

			if (args.length > 0) {
				id = args[0];
			}
			GetItemIdDetailsJsonResponse response = productClient.getItemIdDetailsJson(id);
			JAXBElement<String> object = response.getGetItemIdDetailsJsonResult();
			//System.err.println(response.getGetItemIdDetailsJsonResult());
			//System.out.println(object.getDeclaredType());
			System.out.println(object.getValue());
			//System.out.println(object.getName());
			JSONObject jsonObject = new JSONObject(object.getValue().replace("[","").replace("]",""));
			System.out.println(jsonObject.get("ITEMNAME"));
		};
	}*/

}
