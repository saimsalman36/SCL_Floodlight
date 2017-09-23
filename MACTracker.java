package net.floodlightcontroller.mactracker;
 
import java.util.Collection;
import java.util.Map;
 
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.MacAddress;
 
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

// Dependencies
import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Important
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
// import net.floodlightcontroller.core.internal.OFSwitchManager;

// import core.projectfloodlight.internal.OFSwitchManager;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

class NetworkX {
    Map<String, List<String>> graph;

    public NetworkX() {
        graph = new HashMap<String, List<String>>();
    }

    public void printGraph() {
        for (Map.Entry<String, List<String>> entry : this.graph.entrySet()) {
            System.out.print(entry.getKey() + ": ");

            List<String> temp = entry.getValue();

            if (temp != null) {
                for (Iterator<String> it = temp.iterator(); it.hasNext();) {
                    System.out.print(it.next() + ", ");
                }
                System.out.println();
            }
        }
    }

    public void addNode(String name) {
        if (hasNode(name) == false) {
            List<String> temp = new ArrayList<String>();
            this.graph.put(name, temp);
        } else {
            System.out.println("NOT POSSIBLE!");
            // Throw Exception.
        }
    }

    public void removeNode(String name) {
        if (hasNode(name) == true) {
            List<String> copy = new ArrayList<String>(this.graph.get(name));
            for (int i = 0; i < copy.size(); i++) {
                removeEdge(name, copy.get(i));
            }   
            this.graph.remove(name);
        } else {
            System.out.println("NOT POSSIBLE!");
            // Throw Exception.
        }
    }

    public boolean hasEdge(String node1, String node2) {
        if (hasNode(node1) == false || hasNode(node2) == false) {
            return false;
        }

        return this.graph.get(node1).contains(node2) && this.graph.get(node2).contains(node1);
    }

    public void addEdge(String node1, String node2) {
        if (hasNode(node1) == false || hasNode(node2) == false) {
            // Throw Exception
            System.out.println("NOT POSSIBLE");
        }

        if (hasEdge(node1, node2) != true) {
            this.graph.get(node1).add(node2);
            this.graph.get(node2).add(node1);
        } else {
            // System.out.println("Edge already present");
        }
    }

    public void removeEdge(String node1, String node2) {
        if (hasEdge(node1, node2) == true) {
            this.graph.get(node1).remove(node2);
            this.graph.get(node2).remove(node1);
        } else {
            // Throw Exception!
            System.out.println("NOT POSSIBLE!");
        }
    }

    public boolean hasNode(String name) {
        if (this.graph.size() == 0) {
            return false;
        }
        
        for (Map.Entry<String, List<String>> entry : this.graph.entrySet()) {
            if (entry.getKey().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public List<List<String>> printAllPaths(String node1, String node2) {
        Map<String, Boolean> visited = new HashMap<String, Boolean>();

        for (Map.Entry<String, List<String>> entry : this.graph.entrySet()) {
            visited.put(entry.getKey(), false);
        }

        List<String> path = new ArrayList<String>();
        List<List<String>> res = new ArrayList<List<String>>();
        printAllPaths_(node1, node2, visited, path, res);

        return res;
    }

    private void printAllPaths_(String u, String d, Map<String, Boolean> visited, List<String> path, List<List<String>> res) {
        visited.put(u, true);
        path.add(u);

        if (u == d) {
            List<String> newList = new ArrayList<String>();

            for (String str : path) {
                newList.add(str);   
            }

            res.add(newList);
        } else {
            List<String> temp = this.graph.get(u);

            for (int i = 0; i < temp.size(); i++) {
                if (visited.get(temp.get(i)) == false) {
                    printAllPaths_(temp.get(i), d, visited, path, res);
                }
            }
        }

        path.remove(path.size() - 1);
        visited.put(u, false);
    }
}

class Link {
    String sw1;
    String intf1;
    int port1;
    int state1;
    String sw2;
    String intf2;
    int port2;
    int state2;

    public Link(String sw1, String intf1, String sw2, String intf2, int port1, int port2) {
        this.sw1 = sw1;
        this.intf1 = intf1;
        this.sw2 = sw2;
        this.intf2 = intf2;
        this.port1 = port1;
        this.port2 = port2;

        if (this.sw1.charAt(0) == 'h') {
            this.state1 = 512; // represents OFPPS_STP_FORWARD [POX].
            this.port1 = 1; // Because it's a host hence port 1.
        }

        if (this.sw2.charAt(0) == 'h') {
            this.state2 = 512; // represents OFPPS_STP_FORWARD [POX].
            this.port2 = 1; // Because it's a host hence port 1.
        }
    }

    public Link(String sw1, String intf1, String sw2, String intf2) {
        this.sw1 = sw1;
        this.intf1 = intf1;
        this.sw2 = sw2;
        this.intf2 = intf2; 
        this.port1 = -1;
        this.port2 = -1;
        this.state1 = 1; // 1 represents link down. of.OFPPS_LINK_DOWN in POX.
        this.state2 = 1; // 1 represents link down. of.OFPPS_LINK_DOWN in POX.
    }
}

class Graph {
    public Map<String, IPv4Address> switches;
    public List<String> ctrls;
    public List<Link> links;
    public Map<String, IPv4Address> hosts;
    public Map<String, Link> intf2link;
    public Map<String, Map<String, Link>> sw2link;
    public NetworkX networkGraph;

    public Graph() {
        switches = new HashMap<String, IPv4Address>();
        ctrls = new ArrayList<String>();
        links = new ArrayList<Link>();
        hosts = new HashMap<String, IPv4Address>();
        intf2link = new HashMap<String, Link>();
        sw2link = new HashMap<String, Map<String, Link>>();
        networkGraph = new NetworkX();
    }
}

class JSONParser {
    public static Graph loadTopology(String jsonData) throws IOException{
        JsonParser jsonParser = new JsonFactory().createParser(jsonData);
        Graph g = new Graph();

        jsonParser.nextToken();
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected START_OBJECT");
        }

        while(jsonParser.nextToken() != JsonToken.END_OBJECT) {
            if (jsonParser.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new IOException("Expected FIELD_NAME");
            }

            String name = jsonParser.getCurrentName();

            if ("switches".equals(name)) {
                jsonParser.nextToken();
        
                if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                    throw new IOException("Expected START_OBJECT");
                }

                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    jsonParser.nextToken();
                    g.switches.put(jsonParser.getCurrentName(), IPv4Address.of(jsonParser.getText()));
                }
            } else if ("hosts".equals(name)) {
                jsonParser.nextToken();
        
                if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                    throw new IOException("Expected START_OBJECT");
                }

                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    jsonParser.nextToken();
                    g.hosts.put(jsonParser.getCurrentName(), IPv4Address.of(jsonParser.getText()));
                }
            } else if ("ctrls".equals(name)) {
                jsonParser.nextToken();

                if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                    throw new IOException("Expected START_ARRAY");
                }

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    g.ctrls.add(jsonParser.getText());
                }
            } else if ("links".equals(name)) {
                jsonParser.nextToken();

                if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                    throw new IOException("Expected START_ARRAY");
                }

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                        throw new IOException("Expected START_ARRAY");
                    }

                    int counter = 0;
                    ArrayList<String> tempStrings = new ArrayList<String>();

                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        tempStrings.add(jsonParser.getText());
                        counter = counter + 1;
                    }
                    Link tempLink = null;

                    if (counter == 4) {
                        // SW1, INTF1, SW2, INTF2
                        tempLink = new Link(tempStrings.get(0), tempStrings.get(1), tempStrings.get(2), tempStrings.get(3));

                        g.links.add(tempLink);
                        g.intf2link.put(tempStrings.get(0) + ":" + tempStrings.get(1),tempLink);
                        g.intf2link.put(tempStrings.get(2) + ":" + tempStrings.get(3),tempLink);

                        if (g.sw2link.get(tempStrings.get(0)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(2), tempLink);
                            g.sw2link.put(tempStrings.get(0), temp);
                        } else {
                            g.sw2link.get(tempStrings.get(0)).put(tempStrings.get(2), tempLink);
                        }

                        if (g.sw2link.get(tempStrings.get(2)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(0), tempLink);
                            g.sw2link.put(tempStrings.get(2), temp);
                        } else {
                            g.sw2link.get(tempStrings.get(2)).put(tempStrings.get(0), tempLink);
                        }

                    } else if (counter == 6) {
                        // SW1, INTF1, PORT1, SW2, INTF2, PORT2
                        tempLink = new Link(tempStrings.get(0), tempStrings.get(1), tempStrings.get(3), tempStrings.get(4), Integer.valueOf(tempStrings.get(2)), Integer.valueOf(tempStrings.get(5)));
                        
                        g.links.add(tempLink);
                        g.intf2link.put(tempStrings.get(0) + ":" + tempStrings.get(1),tempLink);
                        g.intf2link.put(tempStrings.get(3) + ":" + tempStrings.get(4),tempLink);

                        if (g.sw2link.get(tempStrings.get(0)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(3), tempLink);
                            g.sw2link.put(tempStrings.get(0), temp);
                        } else {
                            g.sw2link.get(tempStrings.get(0)).put(tempStrings.get(3), tempLink);
                        }

                        if (g.sw2link.get(tempStrings.get(3)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(0), tempLink);
                            g.sw2link.put(tempStrings.get(3), temp);
                        } else {
                            g.sw2link.get(tempStrings.get(3)).put(tempStrings.get(0), tempLink);
                        }
                    }
                }
            }   
        }

        for (String name: g.hosts.keySet()) {
            if (!g.ctrls.contains(name)) {
                g.networkGraph.addNode(name);
            }
        }

        g.networkGraph.printGraph();

        return g;
    }
}
 
public class MACTracker implements IFloodlightModule, IOFSwitchListener {
    protected Graph g;
    protected IOFSwitchService switchService;
    protected static Logger logger;
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        // l.add(IFloodlightProviderService.class);
        l.add(IOFSwitchService.class);
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {

        switchService = context.getServiceImpl(IOFSwitchService.class);
        logger = LoggerFactory.getLogger(MACTracker.class);
        g = new Graph(); 
    }

    @Override
    public void switchDeactivated(DatapathId switchId) {

    }

    @Override
    public void switchChanged(DatapathId switchId) {

    }

    @Override
    public void switchPortChanged(DatapathId switchId,
                                  OFPortDesc port,
                                  PortChangeType type) {

    }

    @Override
    public void switchActivated(DatapathId switchId) {
        logger.debug("Switch %s up.", switchId.toString());
    }

    @Override
    public void switchRemoved(DatapathId switchId) {

    }

    @Override
    public void switchAdded(DatapathId switchId) {
        logger.info("SWITCH ------------------");
    }
 
    @Override
    public void startUp(FloodlightModuleContext context) {
        switchService.addOFSwitchListener(this);

        try {
            logger.info("Start");
            String fmJson = "{\"switches\": {\"s018\": \"10.0.18.1\", \"s019\": \"10.0.19.1\", \"s012\": \"10.0.12.1\", \"s013\": \"10.0.13.1\", \"s010\": \"10.0.10.1\", \"s011\": \"10.0.11.1\", \"s016\": \"10.0.16.1\", \"s017\": \"10.0.17.1\", \"s014\": \"10.0.14.1\", \"s015\": \"10.0.15.1\", \"s005\": \"10.0.5.1\", \"s004\": \"10.0.4.1\", \"s007\": \"10.0.7.1\", \"s006\": \"10.0.6.1\", \"s001\": \"10.0.1.1\", \"s000\": \"10.0.0.1\", \"s003\": \"10.0.3.1\", \"s002\": \"10.0.2.1\", \"s009\": \"10.0.9.1\", \"s008\": \"10.0.8.1\"}, \"hosts\": {\"h014\": \"10.1.14.1\", \"h008\": \"10.1.8.1\", \"h009\": \"10.1.9.1\", \"h004\": \"10.1.4.1\", \"h005\": \"10.1.5.1\", \"h006\": \"10.1.6.1\", \"h007\": \"10.1.7.1\", \"h000\": \"10.1.0.1\", \"h001\": \"10.1.1.1\", \"h002\": \"10.1.2.1\", \"h003\": \"10.1.3.1\", \"h011\": \"10.1.11.1\", \"h012\": \"10.1.12.1\", \"h013\": \"10.1.13.1\", \"h010\": \"10.1.10.1\", \"h015\": \"10.1.15.1\"}, \"links\": [[\"s011\", \"s011-eth0\", 1, \"s002\", \"s002-eth3\", 4], [\"s005\", \"s005-eth3\", 4, \"s013\", \"s013-eth1\", 2], [\"s007\", \"s007-eth1\", 2, \"s003\", \"s003-eth1\", 2], [\"s009\", \"s009-eth0\", 1, \"s002\", \"s002-eth2\", 3], [\"s006\", \"s006-eth0\", 1, \"s000\", \"s000-eth1\", 2], [\"s010\", \"s010-eth0\", 1, \"s000\", \"s000-eth3\", 4], [\"s017\", \"s017-eth2\", 3, \"h010\", \"h010-eth0\", 1], [\"s011\", \"s011-eth3\", 4, \"s019\", \"s019-eth1\", 2], [\"s005\", \"s005-eth1\", 2, \"s003\", \"s003-eth0\", 1], [\"s007\", \"s007-eth3\", 4, \"s015\", \"s015-eth1\", 2], [\"s011\", \"s011-eth1\", 2, \"s003\", \"s003-eth3\", 4], [\"s008\", \"s008-eth2\", 3, \"s016\", \"s016-eth0\", 1], [\"s008\", \"s008-eth3\", 4, \"s017\", \"s017-eth0\", 1], [\"s008\", \"s008-eth0\", 1, \"s000\", \"s000-eth2\", 3], [\"s010\", \"s010-eth1\", 2, \"s001\", \"s001-eth3\", 4], [\"s004\", \"s004-eth0\", 1, \"s000\", \"s000-eth0\", 1], [\"s015\", \"s015-eth2\", 3, \"h006\", \"h006-eth0\", 1], [\"s010\", \"s010-eth3\", 4, \"s019\", \"s019-eth0\", 1], [\"s009\", \"s009-eth3\", 4, \"s017\", \"s017-eth1\", 2], [\"s014\", \"s014-eth3\", 4, \"h005\", \"h005-eth0\", 1], [\"s006\", \"s006-eth3\", 4, \"s015\", \"s015-eth0\", 1], [\"s005\", \"s005-eth2\", 3, \"s012\", \"s012-eth1\", 2], [\"s005\", \"s005-eth0\", 1, \"s002\", \"s002-eth0\", 1], [\"s014\", \"s014-eth2\", 3, \"h004\", \"h004-eth0\", 1], [\"s011\", \"s011-eth2\", 3, \"s018\", \"s018-eth1\", 2], [\"s013\", \"s013-eth2\", 3, \"h002\", \"h002-eth0\", 1], [\"s004\", \"s004-eth2\", 3, \"s012\", \"s012-eth0\", 1], [\"s016\", \"s016-eth3\", 4, \"h009\", \"h009-eth0\", 1], [\"s018\", \"s018-eth3\", 4, \"h013\", \"h013-eth0\", 1], [\"s008\", \"s008-eth1\", 2, \"s001\", \"s001-eth2\", 3], [\"s006\", \"s006-eth2\", 3, \"s014\", \"s014-eth0\", 1], [\"s009\", \"s009-eth1\", 2, \"s003\", \"s003-eth2\", 3], [\"s019\", \"s019-eth2\", 3, \"h014\", \"h014-eth0\", 1], [\"s004\", \"s004-eth1\", 2, \"s001\", \"s001-eth0\", 1], [\"s006\", \"s006-eth1\", 2, \"s001\", \"s001-eth1\", 2], [\"s010\", \"s010-eth2\", 3, \"s018\", \"s018-eth0\", 1], [\"s009\", \"s009-eth2\", 3, \"s016\", \"s016-eth1\", 2], [\"s012\", \"s012-eth2\", 3, \"h000\", \"h000-eth0\", 1], [\"s012\", \"s012-eth3\", 4, \"h001\", \"h001-eth0\", 1], [\"s018\", \"s018-eth2\", 3, \"h012\", \"h012-eth0\", 1], [\"s007\", \"s007-eth0\", 1, \"s002\", \"s002-eth1\", 2], [\"s004\", \"s004-eth3\", 4, \"s013\", \"s013-eth0\", 1], [\"s007\", \"s007-eth2\", 3, \"s014\", \"s014-eth1\", 2], [\"s017\", \"s017-eth3\", 4, \"h011\", \"h011-eth0\", 1]], \"ctrls\": [\"h003\", \"h007\", \"h008\", \"h015\"]}";
            JSONParser.loadTopology(fmJson);    
        } catch (IOException e) {
            logger.info("FAILED");
        } 
    }
}