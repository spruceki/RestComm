/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.restcomm.telephony.ua;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.mobicents.servlet.restcomm.dao.DaoManager;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
public final class UserAgentManagerProxy extends SipServlet {
  private static final long serialVersionUID = 1L;
  
  private ActorSystem system;
  private ActorRef manager;
  

  public UserAgentManagerProxy() {
    super();
  }
  
  @Override public void destroy() {
    system.stop(manager);
  }

  @Override protected void doRequest(final SipServletRequest request)
      throws ServletException, IOException {
    manager.tell(request, null);
  }

  @Override protected void doResponse(final SipServletResponse response)
      throws ServletException, 	IOException {
    manager.tell(response, null);
  }
  
  @Override public void init(final ServletConfig config) throws ServletException {
    final ServletContext context = config.getServletContext();
    final SipFactory factory = (SipFactory)context.getAttribute(SIP_FACTORY);
    final DaoManager storage = (DaoManager)context.getAttribute(DaoManager.class.getName());
    system = (ActorSystem)context.getAttribute(ActorSystem.class.getName());
    manager = manager(config, factory, storage);
  }
  
  private ActorRef manager(final ServletConfig configuration,
      final SipFactory factory, final DaoManager storage) {
    return system.actorOf(new Props(new UntypedActorFactory() {
		private static final long serialVersionUID = 1L;
		@Override public UntypedActor create() throws Exception {
          return new UserAgentManager(configuration, factory, storage);
		}
    }));
  }
}