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
import net.floodlightcontroller.core.internal.IOFSwitchManager;

import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;

// import core.projectfloodlight.internal.IOFSwitchManager;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

class Triples {
    String host1;
    String host2;
    Link lnk;

    public Triples(String host1, String host2, Link lnk) {
        this.host1 = host1;
        this.host2 = host2;
        this.lnk = lnk;
    }

    public String toString() {
        return "Host-1: " + this.host1 + ", Host-2: " + this.host2;
    }
}

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

        if (u.equals(d)) {
            List<String> newList = new ArrayList<String>();

            for (String str : path) {
                newList.add(str);   
            }

            res.add(newList);
        } else {
            List<String> temp = this.graph.get(u);

            if (temp != null) {
                for (int i = 0; i < temp.size(); i++) {
                    if (visited.get(temp.get(i)) == false) {
                        printAllPaths_(temp.get(i), d, visited, path, res);
                    }
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
    int state2; //TODO: Change State from an Integer.

    public Link(String sw1, String intf1, String sw2, String intf2, int port1, int port2) {
        this.sw1 = sw1;
        this.intf1 = intf1;
        this.sw2 = sw2;
        this.intf2 = intf2;
        this.port1 = port1;
        this.port2 = port2;
        this.state1 = 1;
        this.state2 = 1;

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

    public String toString() {
        return "Switch SW1: " + sw1 + ", Interface-1: " + intf1 + ", Switch SW2: " + sw2 + ", Interface-2: " + intf2 + ", Port-1: " + port1 + ", Port-2: " + port2 + ", State-1: " + state1 + ", State-2: " + state2;
    }
}
 
public class MACTracker implements IFloodlightModule, IOFSwitchListener, ILinkDiscoveryListener {
    protected static Map<String, IPv4Address> switches;
    protected static List<String> ctrls;
    protected static List<Link> links;
    protected static Map<String, Map<String, Map<String, Link>>> sw_tables;
    protected static Map<String, Map<String, Map<String, String>>> sw_tables_status;
    protected static Map<String, IPv4Address> hosts;
    protected static Map<String, Link> intf2link;
    protected static Map<String, Map<String, Link>> sw2link;
    protected static NetworkX networkGraph;
    protected static Map<String, IOFSwitch> swToConn;

    protected ILinkDiscoveryService linkService;
    protected IOFSwitchService switchService;
    protected static Logger logger;

    public void loadTopology(String jsonData) throws IOException{
        JsonParser jsonParser = new JsonFactory().createParser(jsonData);

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
                    this.switches.put(jsonParser.getCurrentName(), IPv4Address.of(jsonParser.getText()));
                }
            } else if ("hosts".equals(name)) {
                jsonParser.nextToken();
        
                if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                    throw new IOException("Expected START_OBJECT");
                }

                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    jsonParser.nextToken();
                    this.hosts.put(jsonParser.getCurrentName(), IPv4Address.of(jsonParser.getText()));
                }
            } else if ("ctrls".equals(name)) {
                jsonParser.nextToken();

                if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                    throw new IOException("Expected START_ARRAY");
                }

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    this.ctrls.add(jsonParser.getText());
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

                        this.links.add(tempLink);
                        this.intf2link.put(tempStrings.get(0) + ":" + tempStrings.get(1),tempLink);
                        this.intf2link.put(tempStrings.get(2) + ":" + tempStrings.get(3),tempLink);

                        if (this.sw2link.get(tempStrings.get(0)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(2), tempLink);
                            this.sw2link.put(tempStrings.get(0), temp);
                        } else {
                            this.sw2link.get(tempStrings.get(0)).put(tempStrings.get(2), tempLink);
                        }

                        if (this.sw2link.get(tempStrings.get(2)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(0), tempLink);
                            this.sw2link.put(tempStrings.get(2), temp);
                        } else {
                            this.sw2link.get(tempStrings.get(2)).put(tempStrings.get(0), tempLink);
                        }

                    } else if (counter == 6) {
                        // SW1, INTF1, PORT1, SW2, INTF2, PORT2
                        tempLink = new Link(tempStrings.get(0), tempStrings.get(1), tempStrings.get(3), tempStrings.get(4), Integer.valueOf(tempStrings.get(2)), Integer.valueOf(tempStrings.get(5)));
                        
                        this.links.add(tempLink);
                        this.intf2link.put(tempStrings.get(0) + ":" + tempStrings.get(1),tempLink);
                        this.intf2link.put(tempStrings.get(3) + ":" + tempStrings.get(4),tempLink);

                        if (this.sw2link.get(tempStrings.get(0)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(3), tempLink);
                            this.sw2link.put(tempStrings.get(0), temp);
                        } else {
                            this.sw2link.get(tempStrings.get(0)).put(tempStrings.get(3), tempLink);
                        }

                        if (this.sw2link.get(tempStrings.get(3)) == null) {
                            Map<String, Link> temp = new HashMap<String, Link>();
                            temp.put(tempStrings.get(0), tempLink);
                            this.sw2link.put(tempStrings.get(3), temp);
                        } else {
                            this.sw2link.get(tempStrings.get(3)).put(tempStrings.get(0), tempLink);
                        }
                    }
                }
            }   
        }

        for (String name: this.hosts.keySet()) {
            if (!this.ctrls.contains(name)) {
                logger.info(name);
                this.networkGraph.addNode(name);
            }
        }
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }
 
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(ILinkDiscoveryService.class);
        l.add(IOFSwitchService.class);
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {

        switchService = context.getServiceImpl(IOFSwitchService.class);
        linkService = context.getServiceImpl(ILinkDiscoveryService.class);
        logger = LoggerFactory.getLogger(MACTracker.class);
        
        switches = new HashMap<String, IPv4Address>();
        ctrls = new ArrayList<String>();
        links = new ArrayList<Link>();
        hosts = new HashMap<String, IPv4Address>();
        intf2link = new HashMap<String, Link>();
        sw2link = new HashMap<String, Map<String, Link>>();
        networkGraph = new NetworkX();
        swToConn = new HashMap<String, IOFSwitch>();
        sw_tables = new HashMap<String, Map<String, Map<String, Link>>>();
        sw_tables_status = new HashMap<String, Map<String, Map<String, String>>>();
    }

    public String switchID_to_string(DatapathId switchId) {
        String temp = switchId.toString().replace(":", "");
        return ("s" + temp.substring(temp.length()-3,temp.length()));
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
        if (type == PortChangeType.DOWN) {
            String swID = switchID_to_string(switchId);
            logger.info(swID + ':' + port.getName());
            Link lnk = intf2link.get(swID + ':' + port.getName());

            String sw1 = lnk.sw1;
            String sw2 = lnk.sw2;
       
            if (sw1.equals(swID)) {
                lnk.state1 = 1;

                if (lnk.state2 == 1) {
                    return;
                } else {
                    networkGraph.removeEdge(sw1, sw2);
                    // TODO: Update Flow Tables
                }
            } else if (sw2.equals(swID)) {
                lnk.state2 = 1;

                if (lnk.state1 == 1) {
                    return;
                } else {
                    networkGraph.removeEdge(sw1, sw2);
                    // TODO: Update Flow Tables
                }
            }
        }
    }

    @Override
    public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
        // logger.info("------------ UPDATES UPDATES ---------------------");
        for (LDUpdate u : updateList) {
            logger.info("getOperation(): "+ u.getOperation().toString());
            if (u.getOperation() == UpdateOperation.PORT_UP) {
                String swID = switchID_to_string(u.getSrc());
                String interfaceName = swToConn.get(swID).getPort(u.getSrcPort()).getName();
                Link lnk = intf2link.get(swID + ':' + interfaceName);

                if (lnk == null) continue;
                if (lnk.sw1.equals(swID)) {
                    lnk.port1 = u.getSrcPort().getPortNumber();
                } else if (lnk.sw2.equals(swID)) {
                    lnk.port2 = u.getSrcPort().getPortNumber();
                } else {
                    logger.info("WHY IS IT COMING HERE?");
                }

                // TODO: Old State Variable
                // TODO: lnk is None?

                String sw1 = lnk.sw1;
                String sw2 = lnk.sw2;

                if (sw1.equals(swID)) {
                    lnk.state1 = 512;

                    if (lnk.state2 == 512) {
                        this.networkGraph.addEdge(sw1, sw2);
                        calShortestRoute();
                        // TODO: Update Flow Tables
                    } else {
                        logger.info("WAIT FOR OTHER PORT.");
                    }
                } else if (sw2.equals(swID)) {
                    lnk.state2 = 512;

                    if (lnk.state1 == 512) {
                        this.networkGraph.addEdge(sw1, sw2);
                        calShortestRoute();
                        // TODO: Update Flow Tables;
                    } else {
                        logger.info("WAIT FOR OTHER PORT.");
                    }
                }
            }
        }
    }

    @Override
    public void switchActivated(DatapathId switchId) {

    }

    void updateFlowEntry(IOFSwitch sw) {
        OFFactory myFactory = sw.getOFFactory();
        OFPort pNorm = OFPort.NORMAL;

        Match myMatch = myFactory.buildMatch()
        .setExact(MatchField.ETH_TYPE, EthType.IPv4)
        .build();

        OFInstructions instructions = myFactory.instructions();
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        OFActions actions = myFactory.actions();
 
        /* Output to a port is also an OFAction, not an OXM. */
        OFActionOutput output = actions.buildOutput()
            .setMaxLen(0xFFFFFFFF)
            .setPort(pNorm)
            .build();
        actionList.add(output);
         
        /* Supply the OFAction list to the OFInstructionApplyActions. */
        OFInstructionApplyActions applyActions = instructions.buildApplyActions()
            .setActions(actionList)
            .build();
        
        
        ArrayList<OFInstruction> myInstructionList = new ArrayList<OFInstruction>();
        myInstructionList.add(applyActions);
        
        
        //FlowMod
        OFFlowAdd flowAdd = myFactory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                // .setHardTimeout(3600)
                // .setIdleTimeout(10)
                .setPriority(50000)
                .setMatch(myMatch)
                .setInstructions(myInstructionList)
                .setOutPort(OFPort.of(1))
                .setTableId(TableId.of(0))
                .build();
             
        sw.write(flowAdd);
    }

    public void updateFlowEntries(Map<String, Map<String, List<Triples>>> updates) {
        if (updates.get("modify").size() == 0 && updates.get("delete").size() == 0) {
            return;
        }

        Integer total = 0;
        Integer modify = 0;
        Integer delete = 0;

        
    }

    public Map<String, Map<String, List<Triples>>> calShortestRoute() {
        Integer current = 0;
        
        Map<String, List<Triples>> tempMap = new HashMap<String, List<Triples>>();
        Map<String, List<Triples>> tempMap_ = new HashMap<String, List<Triples>>();
        
        Map<String, Map<String, List<Triples>>> updates = new HashMap<String, Map<String, List<Triples>>>();
        updates.put("modify", tempMap_);
        updates.put("delete", tempMap);
        //TODO: Map -> That contains modified updates and deleted updates.

        for (String host1 : this.hosts.keySet()) {
            for (String host2 : this.hosts.keySet()) {
                if (host1.equals(host2)) continue;
                List<List<String>> paths = this.networkGraph.printAllPaths(host1, host2);
                if (paths.size() == 0) continue;

                List<String> path = paths.get(current % paths.size());

                for (int i = 1; i < path.size() - 1; i++) {
                    String a = path.get(i);
                    String b = path.get(i + 1);

                    Link lnk = this.sw2link.get(a).get(b);
                    Triples tempTriple = new Triples(host1, host2, lnk);
                    boolean notChecked = true;

                    if (this.sw_tables.get(a) == null) {
                        Map<String, Link> temp = new HashMap<String, Link>();
                        temp.put(host2,lnk);

                        Map<String, String> statusTemp = new HashMap<String, String>();
                        statusTemp.put(host2,"updated");

                        Map<String, Map<String, Link>> temp2 = new HashMap<String, Map<String, Link>>();
                        Map<String, Map<String, String>> statusTemp2 = new HashMap<String, Map<String, String>>();
                        statusTemp2.put(host1,statusTemp);
                        temp2.put(host1, temp);

                        this.sw_tables_status.put(a,statusTemp2);
                        this.sw_tables.put(a, temp2);
                    } else if (this.sw_tables.get(a) != null) {
                        Map<String, Map<String, Link>> temp = this.sw_tables.get(a);
                        Map<String, Map<String, String>> statusTemp = this.sw_tables_status.get(a);

                        if (temp.get(host1) == null) {
                            Map<String, Link> temp2 = new HashMap<String, Link>();
                            Map<String, String> statusTemp2 = new HashMap<String, String>();

                            temp2.put(host2,lnk);
                            statusTemp2.put(host2, "updated");

                            temp.put(host1, temp2);
                            statusTemp.put(host1, statusTemp2);
                        } else if (temp.get(host1) != null) {
                            Map<String, Link> temp2 = temp.get(host1);
                            Map<String, String> statusTemp2 = statusTemp.get(host1);

                            if (temp2.get(host2) == null) {
                                temp2.put(host2,lnk);
                                statusTemp2.put(host2, "updated");
                            } else if (temp2.get(host2) != null) {
                                this.sw_tables_status.get(a).get(host1).put(host2, "checked");
                                notChecked = false;
                            }
                        }
                    }
                    if (notChecked == true) {
                        if (updates.get("modify").get(a) == null) {
                            List<Triples> tmp = new ArrayList<Triples>();
                            tmp.add(tempTriple);
                            updates.get("modify").put(a, tmp);
                        } else {
                            updates.get("modify").get(a).add(tempTriple);
                        }
                    }
                }
            }
        }

        for (String sw : this.sw_tables_status.keySet()) {
           Map<String, Map<String, String>> temp = this.sw_tables_status.get(sw);
            
            for (String host1 : temp.keySet()) {
                Map<String, String> temp_ = temp.get(host1);
                
                for (String host2 : temp_.keySet()) {
                    String res = temp_.get(host2);

                    if ((!res.equals("updated")) && (!res.equals("checked"))) {
                        if (updates.get("delete").get(sw) == null) {
                            List<Triples> tmp = new ArrayList<Triples>();
                            tmp.add(new Triples(host1, host2, this.sw_tables.get(sw).get(host1).get(host2)));
                            updates.get("delete").put(sw, tmp);
                        } else {
                            updates.get("delete").get(sw).add(new Triples(host1, host2, this.sw_tables.get(sw).get(host1).get(host2)));
                        }

                        this.sw_tables_status.get(sw).get(host1).remove(host2);
                        this.sw_tables.get(sw).get(host1).remove(host2);
                    } else {
                        this.sw_tables_status.get(sw).get(host1).put(host2, "to_be_deleted");
                    }
                }    
            }
        }

        logger.info("START OF MAP");
        logger.info(Integer.toString(updates.get("modify").size()));
        logger.info(Integer.toString(sw_tables.size()));
        logger.info(Integer.toString(sw_tables_status.size()));
        logger.info("END OF MAP");
        return updates;
    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        logger.info("Switch down: " + switchID_to_string(switchId));

        String swName = switchID_to_string(switchId);

        if (!this.networkGraph.hasNode(swName)) {
            logger.error("Switch not in current graph. Switch Name: " + swName);
            return;
        }

        this.networkGraph.removeNode(swName);
        swToConn.remove(swName);

        for (Link lnk : sw2link.get(swName).values()) {
            if (swName.equals(lnk.sw1)) lnk.state1 = 1;
            if (swName.equals(lnk.sw2)) lnk.state2 = 1; // TODO: REPLACE THESE NUMBERS WITH FLOODLLIGHT CONSTS.
        }
        // TODO: Update Flow Tables.
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        logger.info("Switch up: " + switchID_to_string(switchId));

        String swName = switchID_to_string(switchId);

        if (this.switches.get(swName) == null) {
            logger.error("Switch not in topology. Switch Name: " + swName);
            return;
        }

        if (this.networkGraph.hasNode(swName)) {
            logger.error("Switch already in current graph. Switch Name: " + swName);
            return;
        }

        IOFSwitch sw = switchService.getActiveSwitch(switchId);
        this.networkGraph.addNode(swName);
        swToConn.put(swName, sw);
    }
 
    @Override
    public void startUp(FloodlightModuleContext context) {
        switchService.addOFSwitchListener(this);
        linkService.addListener(this);

        try {
            // String fmJson = "{\"switches\": {\"s018\": \"10.0.18.1\", \"s019\": \"10.0.19.1\", \"s012\": \"10.0.12.1\", \"s013\": \"10.0.13.1\", \"s010\": \"10.0.10.1\", \"s011\": \"10.0.11.1\", \"s016\": \"10.0.16.1\", \"s017\": \"10.0.17.1\", \"s014\": \"10.0.14.1\", \"s015\": \"10.0.15.1\", \"s005\": \"10.0.5.1\", \"s004\": \"10.0.4.1\", \"s007\": \"10.0.7.1\", \"s006\": \"10.0.6.1\", \"s001\": \"10.0.1.1\", \"s000\": \"10.0.0.1\", \"s003\": \"10.0.3.1\", \"s002\": \"10.0.2.1\", \"s009\": \"10.0.9.1\", \"s008\": \"10.0.8.1\"}, \"hosts\": {\"h014\": \"10.1.14.1\", \"h008\": \"10.1.8.1\", \"h009\": \"10.1.9.1\", \"h004\": \"10.1.4.1\", \"h005\": \"10.1.5.1\", \"h006\": \"10.1.6.1\", \"h007\": \"10.1.7.1\", \"h000\": \"10.1.0.1\", \"h001\": \"10.1.1.1\", \"h002\": \"10.1.2.1\", \"h003\": \"10.1.3.1\", \"h011\": \"10.1.11.1\", \"h012\": \"10.1.12.1\", \"h013\": \"10.1.13.1\", \"h010\": \"10.1.10.1\", \"h015\": \"10.1.15.1\"}, \"links\": [[\"s011\", \"s011-eth0\", 1, \"s002\", \"s002-eth3\", 4], [\"s005\", \"s005-eth3\", 4, \"s013\", \"s013-eth1\", 2], [\"s007\", \"s007-eth1\", 2, \"s003\", \"s003-eth1\", 2], [\"s009\", \"s009-eth0\", 1, \"s002\", \"s002-eth2\", 3], [\"s006\", \"s006-eth0\", 1, \"s000\", \"s000-eth1\", 2], [\"s010\", \"s010-eth0\", 1, \"s000\", \"s000-eth3\", 4], [\"s017\", \"s017-eth2\", 3, \"h010\", \"h010-eth0\", 1], [\"s011\", \"s011-eth3\", 4, \"s019\", \"s019-eth1\", 2], [\"s005\", \"s005-eth1\", 2, \"s003\", \"s003-eth0\", 1], [\"s007\", \"s007-eth3\", 4, \"s015\", \"s015-eth1\", 2], [\"s011\", \"s011-eth1\", 2, \"s003\", \"s003-eth3\", 4], [\"s008\", \"s008-eth2\", 3, \"s016\", \"s016-eth0\", 1], [\"s008\", \"s008-eth3\", 4, \"s017\", \"s017-eth0\", 1], [\"s008\", \"s008-eth0\", 1, \"s000\", \"s000-eth2\", 3], [\"s010\", \"s010-eth1\", 2, \"s001\", \"s001-eth3\", 4], [\"s004\", \"s004-eth0\", 1, \"s000\", \"s000-eth0\", 1], [\"s015\", \"s015-eth2\", 3, \"h006\", \"h006-eth0\", 1], [\"s010\", \"s010-eth3\", 4, \"s019\", \"s019-eth0\", 1], [\"s009\", \"s009-eth3\", 4, \"s017\", \"s017-eth1\", 2], [\"s014\", \"s014-eth3\", 4, \"h005\", \"h005-eth0\", 1], [\"s006\", \"s006-eth3\", 4, \"s015\", \"s015-eth0\", 1], [\"s005\", \"s005-eth2\", 3, \"s012\", \"s012-eth1\", 2], [\"s005\", \"s005-eth0\", 1, \"s002\", \"s002-eth0\", 1], [\"s014\", \"s014-eth2\", 3, \"h004\", \"h004-eth0\", 1], [\"s011\", \"s011-eth2\", 3, \"s018\", \"s018-eth1\", 2], [\"s013\", \"s013-eth2\", 3, \"h002\", \"h002-eth0\", 1], [\"s004\", \"s004-eth2\", 3, \"s012\", \"s012-eth0\", 1], [\"s016\", \"s016-eth3\", 4, \"h009\", \"h009-eth0\", 1], [\"s018\", \"s018-eth3\", 4, \"h013\", \"h013-eth0\", 1], [\"s008\", \"s008-eth1\", 2, \"s001\", \"s001-eth2\", 3], [\"s006\", \"s006-eth2\", 3, \"s014\", \"s014-eth0\", 1], [\"s009\", \"s009-eth1\", 2, \"s003\", \"s003-eth2\", 3], [\"s019\", \"s019-eth2\", 3, \"h014\", \"h014-eth0\", 1], [\"s004\", \"s004-eth1\", 2, \"s001\", \"s001-eth0\", 1], [\"s006\", \"s006-eth1\", 2, \"s001\", \"s001-eth1\", 2], [\"s010\", \"s010-eth2\", 3, \"s018\", \"s018-eth0\", 1], [\"s009\", \"s009-eth2\", 3, \"s016\", \"s016-eth1\", 2], [\"s012\", \"s012-eth2\", 3, \"h000\", \"h000-eth0\", 1], [\"s012\", \"s012-eth3\", 4, \"h001\", \"h001-eth0\", 1], [\"s018\", \"s018-eth2\", 3, \"h012\", \"h012-eth0\", 1], [\"s007\", \"s007-eth0\", 1, \"s002\", \"s002-eth1\", 2], [\"s004\", \"s004-eth3\", 4, \"s013\", \"s013-eth0\", 1], [\"s007\", \"s007-eth2\", 3, \"s014\", \"s014-eth1\", 2], [\"s017\", \"s017-eth3\", 4, \"h011\", \"h011-eth0\", 1]], \"ctrls\": [\"h003\", \"h007\", \"h008\", \"h015\"]}";
            String fmJson = "{\"switches\": {\"s004\": \"10.0.1.1\",\"s002\": \"10.0.0.1\",\"s003\": \"10.0.3.1\",\"s001\": \"10.0.2.1\"},\"hosts\": {\"h004\": \"10.1.4.1\",\"h005\": \"10.1.5.1\",\"h006\": \"10.1.6.1\",\"h007\": \"10.1.7.1\",\"h000\": \"10.1.0.1\",\"h001\": \"10.1.1.1\",\"h002\": \"10.1.2.1\",\"h003\": \"10.1.3.1\"},\"links\": [[\"s001\",\"s001-eth1\",1,\"s002\",\"s002-eth1\",1],[\"s001\",\"s001-eth2\",2,\"s003\",\"s003-eth1\",1],[\"s001\",\"s001-eth3\",3,\"s004\",\"s004-eth1\",1],[\"s002\",\"s002-eth2\",2,\"s003\",\"s003-eth2\",2],[\"s002\",\"s002-eth3\",3,\"s004\",\"s004-eth2\",2],[\"s003\",\"s003-eth3\",3,\"s004\",\"s004-eth3\",3],[\"s001\",\"s001-eth4\",4,\"h000\",\"h000-eth0\",1],[\"s001\",\"s001-eth5\",5,\"h001\",\"h001-eth0\",1],[\"s002\",\"s002-eth4\",4,\"h002\",\"h002-eth0\",1],[\"s003\",\"s003-eth4\",4,\"h004\",\"h004-eth0\",1],[\"s003\",\"s003-eth5\",5,\"h005\",\"h005-eth0\",1],[\"s004\",\"s004-eth4\",4,\"h006\",\"h006-eth0\",1]],\"ctrls\": [\"h003\",\"h007\"]}";
            loadTopology(fmJson);    
        } catch (IOException e) {
            logger.info("FAILED");
        } 
    }
}