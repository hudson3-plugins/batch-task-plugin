/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Erik Ramfelt, Koichi Fujikawa, Red Hat, Inc., Seiji Sogabe,
 * Stephen Connolly, Tom Huybrechts, Yahoo! Inc., Alan Harder, CloudBees, Inc.,
 * Yahoo!, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.batch_task;

import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import java.util.StringTokenizer;

/**
 * Adapted from Jenkins getItem to work with Hudson.
 */
public class HudsonUtil {

    public static final <T extends Item> T getItem(Hudson hudson, String pathName, ItemGroup context, Class<T> type) {
        Item r = getItem(hudson, pathName, context);
        if (type.isInstance(r))
            return type.cast(r);
        return null;
    }

    public static final <T extends Item> T getItem(Hudson hudson, String pathName, Item context, Class<T> type) {
        return getItem(hudson, pathName,context!=null?context.getParent():null,type);
    }

    public static Item getItem(Hudson hudson, String pathName, ItemGroup context) {
        if (context==null)  context = hudson;
        if (pathName==null) return null;

        if (pathName.startsWith("/"))   // absolute
            return hudson.getItemByFullName(pathName);

        Object/*Item|ItemGroup*/ ctx = context;

        StringTokenizer tokens = new StringTokenizer(pathName,"/");
        while (tokens.hasMoreTokens()) {
            String s = tokens.nextToken();
            if (s.equals("..")) {
                if (ctx instanceof Item) {
                    ctx = ((Item)ctx).getParent();
                    continue;
                }

                ctx=null;    // can't go up further
                break;
            }
            if (s.equals(".")) {
                continue;
            }

            if (ctx instanceof ItemGroup) {
                ItemGroup g = (ItemGroup) ctx;
                Item i = g.getItem(s);
                if (i==null || !i.hasPermission(Item.READ)) {
                    ctx=null;    // can't go up further
                    break;
                }
                ctx=i;
            } else {
                return null;
            }
        }

        if (ctx instanceof Item)
            return (Item)ctx;

        // fall back to the classic interpretation
        return hudson.getItemByFullName(pathName);
    }
}
