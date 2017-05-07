import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.jms.*;
import javax.servlet.http.HttpSession;
import java.util.List;

@ManagedBean
@SessionScoped
@Named("listcontroller")
public class ListControllerBean implements MessageListener {
    final static String JMS_USERNAME="user";
    final static String JMS_PASSWORD="user";
    @Resource(mappedName = "java:/myJmsTest/MyConnectionFactory")
    private TopicConnectionFactory cf;
    @Resource(mappedName = "java:/myJmsTest/MyTopic")
    private Topic topicExample;
    private TopicConnection connection;
    private TopicSession session;

    private List<Integer> topics;


    @EJB(lookup = "java:global/repository-test/RepositoryTestBean!RepositoryRemote")
    RepositoryRemote repositoryRemote;

    @PostConstruct
    public void initializeTopic(){
        try {
            connection = cf.createTopicConnection(JMS_USERNAME,JMS_PASSWORD);
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicSubscriber subscriber = session.createSubscriber(topicExample);
//            subscriber.setMessageListener(this);

            FacesContext fCtx = FacesContext.getCurrentInstance();
            HttpSession httpSession = (HttpSession) fCtx.getExternalContext().getSession(false);
            String sessionId = httpSession.getId();

            connection.start();

            Message msg = subscriber.receive(10000);
            if (msg == null) {
                System.out.println(sessionId + ": Timed out waiting for msg");
            } else {
                System.out.println(sessionId + ": TopicSubscriber.recv, msgt="+msg);
            }

        }
        catch (Exception exc) {
            System.out.println("Błąd w odbieraniu wiadomosci:" + exc.toString());
        }
    }

    @PreDestroy
    public void connectionHandle() {
        System.out.println("connection destroyed");
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


    public void subsribe(int topic) {
        System.out.println("Subsribing topic: " + topic);
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("Zadanie 1 klient dostał wiadomość: " + message);

    }

    /*-------------------------------------------------------------------------------*/
    public List<Integer> getTopics() {
        return repositoryRemote.getTopics();
    }
}
