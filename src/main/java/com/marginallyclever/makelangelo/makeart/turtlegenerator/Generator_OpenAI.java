package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectPassword;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Generator_OpenAI extends TurtleGenerator {

    private static final Logger logger = LoggerFactory.getLogger(Generator_OpenAI.class);
    private static String APIKey = "";
    private static final String prefix = "Tersely use gcode to trace a vector image in the z=0 plane.  I want to draw: ";
    private static String prompt = "";

    public Generator_OpenAI() {
        super();

        Preferences preferences = Preferences.userNodeForPackage(Generator_OpenAI.class);
        assert preferences != null;
        APIKey = preferences.get("APIKey", APIKey);

        SelectPassword APIField = new SelectPassword("APIKey", Translator.get("Generator_OpenAI.APIKey"), APIKey);
        add(APIField);
        SelectTextArea promptField = new SelectTextArea("description", Translator.get("Generator_OpenAI.prompt"), prompt);
        add(promptField);
        SelectButton generateNow = new SelectButton("generate", Translator.get("Generator_OpenAI.generate"));
        add(generateNow);
        generateNow.addPropertyChangeListener(evt->{
            prompt = promptField.getText();
            generate();
        });

        APIField.addPropertyChangeListener(evt->{
            APIKey = APIField.getPassword();
            preferences.put("APIKey", APIKey);
            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
        });

        add(new SelectReadOnlyText("url","<a href='https://platform.openai.com/docs/quickstart/build-your-application'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
    }

    @Override
    public String getName() {
        return "OpenAI";
    }

    @Override
    public void generate() {
        if(prompt.trim().isEmpty()) return;

        logger.debug("You say: "+prefix+prompt);
        String result ="";
        try {
            result = getResultsFromChatGPT();
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error("OpenAI failed. "+e.getMessage());
            return;
        }
        logger.debug("OpenAI says: "+result);
    }

    private String getResultsFromChatGPT() throws Exception {
        String url = "https://api.openai.com/v1/completions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + APIKey);

        JSONObject data = new JSONObject();
        data.put("model", "text-davinci-003"); // GET https://api.openai.com/v1/models/{model}
        data.put("prompt", prefix+prompt);
        data.put("max_tokens", 4000);
        data.put("temperature", 1.0);

        con.setDoOutput(true);
        con.getOutputStream().write(data.toString().getBytes());

        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                .reduce((a, b) -> a + b).get();

        String result = new JSONObject(output).getJSONArray("choices").getJSONObject(0).getString("text");
        return result;
    }
}
