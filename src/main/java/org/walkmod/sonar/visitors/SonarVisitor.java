package org.walkmod.sonar.visitors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

@RequiresSemanticAnalysis(optional = true)

@SuppressWarnings("rawtypes")
public class SonarVisitor extends VoidVisitorAdapter<VisitorContext> {

    private String authToken;

    private String qualityProfile = "Sonar way";

    private String sonarHost = "https://sonarqube.com";

    private List<String> ruleSet = null;

    private List<VoidVisitorAdapter> visitors = null;

    @SuppressWarnings("unchecked")
    @Override
    public void visit(CompilationUnit n, VisitorContext vc) {

        List<VoidVisitorAdapter> visitors = getVisitors(vc);
        for (VoidVisitorAdapter visitor : visitors) {
            visitor.visit(n, vc);

        }
    }

    public List<String> getRuleSet() {
        if (ruleSet == null) {
            String[] urls = new String[] { "api/qualityprofiles/export", "api/profiles/index", "api/profiles" };
            String response = null;
            try {
                for (int i = 0; i < urls.length && response == null; i++) {
                    response = getProfile(urls[i]);
                }
                if (response != null) {
                   
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document xml = dBuilder.parse(new ByteArrayInputStream(response.getBytes()));
                    ruleSet = parseXML(xml);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ruleSet;
    }

    public List<VoidVisitorAdapter> getVisitors(VisitorContext ctx) {
        if (visitors == null) {
            visitors = new LinkedList<VoidVisitorAdapter>();
            List<String> ruleSet = getRuleSet();
            Properties properties = new Properties();
            try {
                properties.load(ctx.getClassLoader().getResourceAsStream("sonar-fixings.properties"));
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            for (String rule : ruleSet) {

                Object o = null;
                try {
                    if (properties.containsKey(rule)) {

                        Class<?> c = Class.forName("org.walkmod.sonar.visitors." + properties.getProperty(rule), true,
                                ctx.getClassLoader());
                        o = c.newInstance();
                        visitors.add((VoidVisitorAdapter) o);
                    }
                } catch (Exception e) {
                }
            }
        }
        return visitors;
    }

    protected List<String> parseXML(Document xml) {
        List<String> rules = new LinkedList<String>();
        NodeList children = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("rules".equals(child.getNodeName())) {
                NodeList ruleNodes = child.getChildNodes();
                for (int j = 0; j < ruleNodes.getLength(); j++) {
                    Node ruleNode = ruleNodes.item(j);
                    NodeList attrList = ruleNode.getChildNodes();

                    for (int k = 0; k < attrList.getLength(); k++) {
                        Node attrNode = attrList.item(k);
                        if ("key".equals(attrNode.getNodeName())) {
                            rules.add(attrNode.getTextContent());
                        }
                    }

                }
            }
        }
        return rules;
    }

    protected String getProfile(String url) throws UnirestException {
        if (!sonarHost.endsWith("/")) {
            sonarHost += "/";
        }

        HttpRequest request = Unirest.get(sonarHost + url).queryString("language", "java")
                .queryString("name", qualityProfile).basicAuth(authToken, "");
        int code = request.asBinary().getStatus();
        if (code == 200) {
            return request.asString().getBody();
        }
        return null;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getQualityProfile() {
        return qualityProfile;
    }

    public void setQualityProfile(String qualityProfile) {
        this.qualityProfile = qualityProfile;
    }

    public String getSonarHost() {
        return sonarHost;
    }

    public void setSonarHost(String sonarHost) {
        this.sonarHost = sonarHost;
    }

}
