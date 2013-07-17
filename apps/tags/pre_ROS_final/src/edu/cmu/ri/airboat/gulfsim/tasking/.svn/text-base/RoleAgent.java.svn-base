/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking;

import edu.cmu.ri.airboat.gulfsim.BoatSimpleProxy;
import edu.cmu.ri.airboat.gulfsim.ProxyManager;
import edu.cmu.ri.airboat.gulfsim.tasking.PlanAgent;
import edu.cmu.ri.airboat.gulfsim.tasking.pollution.RepeatVisitRole;

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
                (new Thread()  {

                    public void run() {
                        try {
                            sleep(2000);
                        } catch (Exception e) {
                        }
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

        if (role instanceof RepeatVisitRole) {
            // Sleep for a while
            (new Thread()  {

                public void run() {
                    try {
                        sleep(10000);
                    } catch (Exception e) {
                    }
                    
                    // Try to allocate
                    allocated = false;                    
                    execute();
                }
            }).start();
        }
    }
}
