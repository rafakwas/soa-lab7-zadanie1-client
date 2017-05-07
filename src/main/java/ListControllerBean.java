import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.*;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
@Named("listcontroller")
public class ListControllerBean{
    final static String JMS_USERNAME="user";
    final static String JMS_PASSWORD="user";
    @Resource(mappedName = "java:/myJmsTest/MyConnectionFactory")
    private TopicConnectionFactory cf;
    @Resource(mappedName = "java:/myJmsTest/MyTopic")
    private Topic topicExample;
    private TopicConnection connection;
    private TopicSession session;

    private List<Integer> subscribedTopics;
    private boolean newMessage;

    @EJB(lookup = "java:global/repository-test/RepositoryTestBean!RepositoryRemote")
    RepositoryRemote repositoryRemote;

//    @PreDestroy
//    public void connectionHandle() {
//        System.out.println("connection destroyed");
//        if (connection != null) {
//            try {
//                connection.stop();
//                session.close();
//                connection.close();
//            } catch (JMSException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    public void subsribe(int topic) {
        System.out.println("Subsribing topic: " + topic);
        System.out.println("adding topic to subscribed list");

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        List<Integer> subscribedTopics = null;
        if(!sessionMap.containsKey("subscribedTopics")) {
            subscribedTopics = new ArrayList<>();
            sessionMap.put("subscribedTopics", subscribedTopics);
        }
        else {
            subscribedTopics = (List<Integer>) sessionMap.get("subscribedTopics");
        }
        subscribedTopics.add(topic);
        sessionMap.put("subscribedTopics", subscribedTopics);
        System.out.println(subscribedTopics);
        System.out.println(refractorListToMatchSql(subscribedTopics));

        unsubsribeTopic();
        subsribeTopic(subscribedTopics);


    }

    public void unsubscribe(int topic) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        List<Integer> subscribedTopics = null;
        if(sessionMap.containsKey("subscribedTopics")) {
            subscribedTopics = (List<Integer>) sessionMap.get("subscribedTopics");
            if (!subscribedTopics.isEmpty()) {
                subscribedTopics.remove((Object)topic);
            }
        }

        System.out.println(subscribedTopics);

        unsubsribeTopic();

        if(!subscribedTopics.isEmpty())
            subsribeTopic(subscribedTopics);


    }

    public String refractorListToMatchSql(List<Integer> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i =0; i < list.size()-1;i++){
            stringBuilder.append("'"+list.get(i)+"'"+",");
        }
        stringBuilder.append("'"+list.get(list.size()-1)+"')");
        return stringBuilder.toString();
    }

    public void subsribeTopic(List<Integer> list) {

        try {
            connection = cf.createTopicConnection(JMS_USERNAME,JMS_PASSWORD);
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            TopicSubscriber subscriber = session.createSubscriber(topicExample,"topicIdentifier IN " + refractorListToMatchSql(list),true);
//            TopicSubscriber subscriber = session.createSubscriber(topicExample);
            FacesContext fCtx = FacesContext.getCurrentInstance();
            HttpSession httpSession = (HttpSession) fCtx.getExternalContext().getSession(false);
            final String sessionId = httpSession.getId();

            MessageListener messageListener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    System.out.println(sessionId + ": message received... " + message);
                    newMessage = true;
                }
            };

            subscriber.setMessageListener(messageListener);
            connection.start();

        }
        catch (Exception exc) {
            System.out.println("Błąd w odbieraniu wiadomosci:" + exc.toString());
        }
    }

    public void unsubsribeTopic() {
        System.out.println("Topics unsubscribed");
        if (connection != null) {
            try {
                connection.stop();
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetNewMessage() {
        newMessage = false;
    }


    /*-------------------------------------------------------------------------------*/
    public List<Integer> getTopics() {
        return repositoryRemote.getTopics();
    }
    public List<Integer> getSubscribedTopics() {return subscribedTopics;}
    public void setSubscribedTopics(List<Integer> subscribedTopics) {this.subscribedTopics = subscribedTopics;}

    public boolean isNewMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }
}
