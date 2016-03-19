/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the COPYING file with this program.
*/
package org.freedesktop.dbus;

import static org.freedesktop.dbus.Gettext._;

import java.lang.reflect.Proxy;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Random;
import java.util.Vector;

import org.freedesktop.DBus;
import org.freedesktop.dbus.exceptions.DBusException;

import cx.ath.matthew.debug.Debug;

/** Handles a peer to peer connection between two applications withou a bus daemon.
 * <p>
 * Signal Handlers and method calls from remote objects are run in their own threads, you MUST handle the concurrency issues.
 * </p>
 */
public class DirectConnection extends AbstractConnection
{
   /**
    * Create a direct connection to another application.
    * @param address The address to connect to. This is a standard D-Bus address, except that the additional parameter 'listen=true' should be added in the application which is creating the socket.
    */
   public DirectConnection(String address) throws DBusException
   {
      super(address);

      try {
         transport = new Transport(addr, AbstractConnection.TIMEOUT);
			connected = true;
      } catch (IOException IOe) {
         if (EXCEPTION_DEBUG && Debug.debug) Debug.print(Debug.ERR, IOe);            
         throw new DBusException(_("Failed to connect to bus ")+IOe.getMessage());
      } catch (ParseException Pe) {
         if (EXCEPTION_DEBUG && Debug.debug) Debug.print(Debug.ERR, Pe);            
         throw new DBusException(_("Failed to connect to bus ")+Pe.getMessage());
      }

      listen();
   }

   /**
    * Creates a bus address for a randomly generated tcp port.
    * @return a random bus address.
    */
   public static String createDynamicTCPSession()
   {
      String address = "tcp:host=localhost";
      int port;
      try {
         ServerSocket s = new ServerSocket();
         s.bind(null);
         port = s.getLocalPort();
         s.close();
      } catch (Exception e) {
         Random r = new Random();
         port = 32768 + (Math.abs(r.nextInt()) % 28232);
      }
      address += ",port="+port;
      address += ",guid="+Transport.genGUID();
      if (Debug.debug) Debug.print("Created Session address: "+address);
      return address;
   }

   /**
    * Creates a bus address for a randomly generated abstract unix socket.
    * @return a random bus address.
    */
   public static String createDynamicSession()
   {
      String address = "unix:";
      String path = "/tmp/dbus-XXXXXXXXXX";
      Random r = new Random();
      do {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < 10; i++) 
            sb.append((char) ((Math.abs(r.nextInt()) % 26) + 65));
         path = path.replaceAll("..........$", sb.toString());
         if (Debug.debug) Debug.print(Debug.VERBOSE, "Trying path "+path);
      } while ((new File(path)).exists());
      address += "abstract="+path;
      address += ",guid="+Transport.genGUID();
      if (Debug.debug) Debug.print("Created Session address: "+address);
      return address;
   }
   DBusInterface dynamicProxy(String path) throws DBusException
   {
      try {
         DBus.Introspectable intro = (DBus.Introspectable) getRemoteObject(path, DBus.Introspectable.class);
         String data = intro.Introspect();
         String[] tags = data.split("[<>]");
         Vector<String> ifaces = new Vector<String>();
         for (String tag: tags) {
            if (tag.startsWith("interface")) {
               ifaces.add(tag.replaceAll("^interface *name *= *['\"]([^'\"]*)['\"].*$", "$1"));
            }
         }
         Vector<Class<? extends Object>> ifcs = new Vector<Class<? extends Object>>();
         for(String iface: ifaces) {
            int j = 0;
            while (j >= 0) {
               try {
                  ifcs.add(Class.forName(iface));
                  break;
               } catch (Exception e) {}
               j = iface.lastIndexOf(".");
               char[] cs = iface.toCharArray();
               if (j >= 0) {
                  cs[j] = '$';
                  iface = String.valueOf(cs);
               }
            }
         }

         if (ifcs.size() == 0) throw new DBusException(_("Could not find an interface to cast to"));

         RemoteObject ro = new RemoteObject(null, path, null, false);
         DBusInterface newi =  (DBusInterface)
            Proxy.newProxyInstance(ifcs.get(0).getClassLoader(), 
                                   ifcs.toArray(new Class[0]),
                                   new RemoteInvocationHandler(this, ro));
         importedObjects.put(newi, ro);
         return newi;
      } catch (Exception e) {
         if (EXCEPTION_DEBUG && Debug.debug) Debug.print(Debug.ERR, e);
         throw new DBusException(MessageFormat.format(_("Failed to create proxy object for {0}; reason: {1}."), new Object[] { path, e.getMessage()}));
      }
   }
   
   DBusInterface getExportedObject(String path) throws DBusException
   {
      ExportedObject o = null;
      synchronized (exportedObjects) {
         o = exportedObjects.get(path);
      }
      if (null != o && null == o.object.get()) {
         unExportObject(path);
         o = null;
      }
      if (null != o) return o.object.get();
      return dynamicProxy(path);
   }

   /** 
       * Return a reference to a remote object. 
       * This method will always refer to the well known name (if given) rather than resolving it to a unique bus name.
       * In particular this means that if a process providing the well known name disappears and is taken over by another process
       * proxy objects gained by this method will make calls on the new proccess.
       * 
       * This method will use bus introspection to determine the interfaces on a remote object and so
       * <b>may block</b> and <b>may fail</b>. The resulting proxy object will, however, be castable
       * to any interface it implements. It will also autostart the process if applicable. Also note
       * that the resulting proxy may fail to execute the correct method with overloaded methods
       * and that complex types may fail in interesting ways. Basically, if something odd happens, 
       * try specifying the interface explicitly.
       * 
       * @param objectpath The path on which the process is exporting the object.
       * @return A reference to a remote object.
       * @throws ClassCastException If type is not a sub-type of DBusInterface
       * @throws DBusException If busname or objectpath are incorrectly formatted.
    */
   public DBusInterface getRemoteObject(String objectpath) throws DBusException
   {
      if (null == objectpath) throw new DBusException(_("Invalid object path: null"));
      
      if (!objectpath.matches(OBJECT_REGEX) || objectpath.length() > MAX_NAME_LENGTH) 
         throw new DBusException(_("Invalid object path: ")+objectpath);
      
      return dynamicProxy(objectpath);
   }

   /** 
       * Return a reference to a remote object. 
       * This method will always refer to the well known name (if given) rather than resolving it to a unique bus name.
       * In particular this means that if a process providing the well known name disappears and is taken over by another process
       * proxy objects gained by this method will make calls on the new proccess.
       * @param objectpath The path on which the process is exporting the object.
       * @param type The interface they are exporting it on. This type must have the same full class name and exposed method signatures
       * as the interface the remote object is exporting.
       * @return A reference to a remote object.
       * @throws ClassCastException If type is not a sub-type of DBusInterface
       * @throws DBusException If busname or objectpath are incorrectly formatted or type is not in a package.
    */
   public DBusInterface getRemoteObject(String objectpath, Class<? extends DBusInterface> type) throws DBusException
   {
      if (null == objectpath) throw new DBusException(_("Invalid object path: null"));
      if (null == type) throw new ClassCastException(_("Not A DBus Interface"));
      
      if (!objectpath.matches(OBJECT_REGEX) || objectpath.length() > MAX_NAME_LENGTH) 
         throw new DBusException(_("Invalid object path: ")+objectpath);
      
      if (!DBusInterface.class.isAssignableFrom(type)) throw new ClassCastException(_("Not A DBus Interface"));

      // don't let people import things which don't have a
      // valid D-Bus interface name
      if (type.getName().equals(type.getSimpleName()))
         throw new DBusException(_("DBusInterfaces cannot be declared outside a package"));
      
      RemoteObject ro = new RemoteObject(null, objectpath, type, false);
      DBusInterface i =  (DBusInterface) Proxy.newProxyInstance(type.getClassLoader(), 
            new Class[] { type }, new RemoteInvocationHandler(this, ro));
      importedObjects.put(i, ro);
      return i;
   }
   protected <T extends DBusSignal> void removeSigHandler(DBusMatchRule rule, DBusSigHandler<T> handler) throws DBusException
   {
      SignalTuple key = new SignalTuple(rule.getInterface(), rule.getMember(), rule.getObject(), rule.getSource());
      synchronized (handledSignals) {
         Vector<DBusSigHandler<? extends DBusSignal>> v = handledSignals.get(key);
         if (null != v) {
            v.remove(handler);
            if (0 == v.size()) {
               handledSignals.remove(key);
            }
         } 
      }
   }
   protected <T extends DBusSignal> void addSigHandler(DBusMatchRule rule, DBusSigHandler<T> handler) throws DBusException
   {
      SignalTuple key = new SignalTuple(rule.getInterface(), rule.getMember(), rule.getObject(), rule.getSource());
      synchronized (handledSignals) {
         Vector<DBusSigHandler<? extends DBusSignal>> v = handledSignals.get(key);
         if (null == v) {
            v = new Vector<DBusSigHandler<? extends DBusSignal>>();
            v.add(handler);
            handledSignals.put(key, v);
         } else
            v.add(handler);
      }
   }
   DBusInterface getExportedObject(String source, String path) throws DBusException
   {
      return getExportedObject(path);
   }
}
