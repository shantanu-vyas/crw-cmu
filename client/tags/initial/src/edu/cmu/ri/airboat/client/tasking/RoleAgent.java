/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.tasking;

import edu.cmu.ri.airboat.client.BoatSimpleProxy;
import edu.cmu.ri.airboat.client.ProxyManager;

/**
 *
 * @author pscerri
 */
public class RoleAgent {

    private final Role role;
    private final PlanAgent pa;
    private ProxyManager proxyManager = new ProxyManager();
    private boolean allocated = false;

    public RoleAgent(Role role, PlanAgent pa) {
        this.role = role;
        this.pa = pa;

        execute();
    }

    public void execute() {
        if (!allocated) {
            BoatSimpleProxy proxy = proxyManager.getRandomProxy();
            if (proxy != null) {
                boolean accept = proxy.offerRole(this);
                if (accept) {
                    allocated = true;
                } else {
                    // @todo Do something when unaccepted
                }
            } else {
                (new Thread() {
                    public void run() {
                        try {
                            sleep(1000);
                        } catch (Exception e) {}
                        execute();
                    }
                }).start();
            }
        }
    }

    public Role getRole() {
        return role;
    }

    // @todo Should depend on TOP
    public boolean canComplete() {
        return true;
    }

    public void done() {
        // @todo Do something with complete role
    }
}
