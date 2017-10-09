import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

class NetworkX {
    Map<String, List<String>> graph;

    public NetworkX() {
        graph = new HashMap<String, List<String>>();
    }

    public String printGraph() {
    	String ret = "";
    	
        for (Map.Entry<String, List<String>> entry : this.graph.entrySet()) {
            ret += (entry.getKey() + ": ");

            List<String> temp = entry.getValue();

            if (temp != null) {
                for (Iterator<String> it = temp.iterator(); it.hasNext();) {
                    ret += (it.next() + ", ");
                }
                ret += '\n';
            }
        }

        return ret;
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

        if (res.size() == 0) return res;

        Collections.sort(res,new Comparator<List<String>>() {
            @Override
            public int compare(List<String> lhs, List<String> rhs) {
                if (lhs.size() < rhs.size()) {
                    return -1;
                } else if (lhs.size() > rhs.size()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        Integer minSizedPath = res.get(0).size();
        List<List<String>> temp = new ArrayList<List<String>>();

        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).size() == minSizedPath) {
                temp.add(res.get(i));
            }
        }

        return temp;
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

    public static void main(String[] args) {
        NetworkX graph = new NetworkX();

        graph.addNode("h000");
        graph.addNode("h001");
        graph.addNode("h002");
        graph.addNode("h003");
        graph.addNode("h004");
        graph.addNode("h005");
        graph.addNode("h006");
        graph.addNode("h007");
        graph.addNode("h008");
        graph.addNode("h009");
        graph.addNode("h010");
        graph.addNode("h011");
        graph.addNode("h012");
        graph.addNode("h013");
        graph.addNode("h014");
        graph.addNode("h015");

        graph.addNode("s000");
        graph.addNode("s001");
        graph.addNode("s002");
        graph.addNode("s003");
        graph.addNode("s004");
        graph.addNode("s005");
        graph.addNode("s006");
        graph.addNode("s007");
        graph.addNode("s008");
        graph.addNode("s009");
        graph.addNode("s010");
        graph.addNode("s011");
        graph.addNode("s012");
        graph.addNode("s013");
        graph.addNode("s014");       
        graph.addNode("s015");
        graph.addNode("s016");
        graph.addNode("s017");
        graph.addNode("s018");
        graph.addNode("s019");

        graph.addEdge("h000", "s012");
        graph.addEdge("h001", "s012");
        graph.addEdge("h002", "s013");
        graph.addEdge("h003", "s013");
        graph.addEdge("h004", "s014");
        graph.addEdge("h005", "s014");
        graph.addEdge("h006", "s015");
        graph.addEdge("h007", "s015");
        graph.addEdge("h008", "s016");
        graph.addEdge("h009", "s016");
        graph.addEdge("h010", "s017");
        graph.addEdge("h011", "s017");
        graph.addEdge("h012", "s018");
        graph.addEdge("h013", "s018");
        graph.addEdge("h014", "s019");
        graph.addEdge("h015", "s019");

        graph.addEdge("s012", "s004");
        graph.addEdge("s013", "s004");
        graph.addEdge("s012", "s005");
        graph.addEdge("s013", "s005");
        graph.addEdge("s014", "s006");
        graph.addEdge("s015", "s006");
        graph.addEdge("s014", "s007");
        graph.addEdge("s015", "s007");
        graph.addEdge("s016", "s008");
        graph.addEdge("s017", "s008");
        graph.addEdge("s016", "s009");
        graph.addEdge("s017", "s009");
        graph.addEdge("s018", "s010");
        graph.addEdge("s019", "s010");
        graph.addEdge("s018", "s011");
        graph.addEdge("s019", "s011");

        graph.addEdge("s004", "s000");
        graph.addEdge("s006", "s000");
        graph.addEdge("s008", "s000");
        graph.addEdge("s010", "s000");

        graph.addEdge("s004", "s001");
        graph.addEdge("s006", "s001");
        graph.addEdge("s008", "s001");
        graph.addEdge("s010", "s001");

        graph.addEdge("s005", "s002");
        graph.addEdge("s007", "s002");
        graph.addEdge("s009", "s002");
        graph.addEdge("s011", "s002");

        graph.addEdge("s005", "s003");
        graph.addEdge("s007", "s003");
        graph.addEdge("s009", "s003");
        graph.addEdge("s011", "s003");

        System.out.println(graph.printGraph());

        List<List<String>> temp = graph.printAllPaths("h000", "h004");
        System.out.println(temp.toString());
    }
}