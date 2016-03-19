/*
   D-Bus Java Implementation
   Copyright (c) 2005-2006 Matthew Johnson

   This program is free software; you can redistribute it and/or modify it
   under the terms of either the GNU Lesser General Public License Version 2 or the
   Academic Free Licence Version 2.1.

   Full licence texts are included in the COPYING file with this program.
*/
package org.freedesktop.dbus.test;

import java.lang.reflect.Type;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DirectConnection;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt16;

public class test_p2p_server implements TestRemoteInterface
{
   public int[][] teststructstruct(TestStruct3 in)
   {
      List<List<Integer>> lli = in.b;
      int[][] out = new int[lli.size()][];
      for (int j = 0; j < out.length; j++) {
         out[j] = new int[lli.get(j).size()];
         for (int k = 0; k < out[j].length; k++)
            out[j][k] = lli.get(j).get(k);
      }
      return out;
   }
   public String getNameAndThrow()
	{ 
		return getName();
	}
   public String getName()
   {
      System.out.println("getName called");
      return "Peer2Peer Server";
   }
   public <T> int frobnicate(List<Long> n, Map<String,Map<UInt16,Short>> m, T v)
   {
      return 3;
   }
   public void throwme() throws TestException
   {
      System.out.println("throwme called");
      throw new TestException("BOO");
   }
   public void waitawhile()
   {
      return;
   }
   public int overload()
   {
      return 1;
   }
   public void sig(Type[] s)
   {
   }
   public void newpathtest(Path p)
   {
   }
	public void reg13291(byte[] as, byte[] bs)
	{
	}
   public Path pathrv(Path a) { return a; }
   public List<Path> pathlistrv(List<Path> a) { return a; }
   public Map<Path,Path> pathmaprv(Map<Path,Path> a) { return a; }
   public boolean isRemote() { return false; }
   public float testfloat(float[] f)
   {
      System.out.println("got float: "+Arrays.toString(f));
      return f[0];
   }

   public static void main(String[] args) throws Exception
   {
      String address = DirectConnection.createDynamicSession();
      //String address = "tcp:host=localhost,port=12344,guid="+Transport.genGUID();
      PrintWriter w = new PrintWriter(new FileOutputStream("address"));
      w.println(address);
      w.flush();
      w.close();
      DirectConnection dc = new DirectConnection(address+",listen=true");
      System.out.println("Connected");
      dc.exportObject("/Test", new test_p2p_server());
   }      
}
