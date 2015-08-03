/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.commands;

import java.util.Hashtable;

/**
 * This class contains methods for facilitating sorting and filtering lists stored in {@link Hashtable}s.
 * 
 * @author Ana Dimova - Initial Contribution
 *
 */
public class Utils {

    /**
     * This method sorts lexicographically and sets an index of the UIDs of the automation objects.
     * 
     * @param objects holds the list with automation objects for sorting and indexing.
     * @return sorted and indexed list with UIDs of the automation objects.
     */
    static Hashtable<String, String> sortList(Hashtable<String, ?> objects) {
        if (objects == null || objects.isEmpty())
            return null;
        String[] uids = new String[objects.size()];
        sort(objects.keySet().toArray(uids), false, false);
        Hashtable<String, String> sorted = new Hashtable<String, String>();
        for (int i = 0; i < uids.length; i++) {
            sorted.put(new Integer(i + 1).toString(), uids[i]);
        }
        return sorted;
    }

    /**
     * This method filters the list with UIDs of the automation objects to correspond to the list with the automation objects.
     * 
     * @param listObjects holds the list with automation objects  for filter criteria.
     * @param listUIDs holds the list with UIDs of the automation objects for filtering.
     * @return filtered list with UIDs of the automation objects.
     */
    static Hashtable<String, String> filterList(Hashtable<String, ?> listObjects, Hashtable<String, String> listUIDs) {
        Hashtable<String, String> filtered = new Hashtable<String, String>();
        for (String id : listUIDs.keySet()) {
            String uid = listUIDs.get(id);
            Object obj = listObjects.get(uid);
            if (obj != null)
                filtered.put(id, uid);
        }
        return filtered;
    }

    private static void sort(String[] array, boolean ignoreCase, boolean backSort) {
        if (backSort) {
            backquickSort(array, 0, array.length, ignoreCase);
        } else {
            quickSort(array, 0, array.length, ignoreCase);
        }
    }

    private static void quickSort(String[] a, int begin, int length, boolean ignoreCase) {
        int i, j, leftLength, rightLength, t;
        String x;
        while (length >= 3) {
            t = length - 1;
            j = t + begin;
            i = (t >> 1) + begin;
            sort3(a, begin, i, j, ignoreCase);
            if (length == 3) {
                return;
            }
            x = a[i];
            i = begin + 1;
            j--;
            do {
                if (ignoreCase) {
                    while (a[i].toLowerCase().compareTo(x.toLowerCase()) < 0) {
                        i++;
                    }
                    while (a[j].toLowerCase().compareTo(x.toLowerCase()) > 0) {
                        j--;
                    }
                } else {
                    while (a[i].compareTo(x) < 0) {
                        i++;
                    }
                    while (a[j].compareTo(x) > 0) {
                        j--;
                    }
                }
                if (i < j) {
                    swap(a, i, j);
                } else {
                    if (i == j) {
                        i++;
                        j--;
                    }
                    break;
                }
            } while (++i <= --j);
            leftLength = (j - begin) + 1;
            rightLength = (begin - i) + length;
            if (leftLength < rightLength) {
                if (leftLength > 1) {
                    quickSort(a, begin, leftLength, ignoreCase);
                }
                begin = i;
                length = rightLength;
            } else {
                if (rightLength > 1) {
                    quickSort(a, i, rightLength, ignoreCase);
                }
                length = leftLength;
            }
        }
        if (ignoreCase) {
            if (length == 2 && a[begin].toLowerCase().compareTo(a[begin + 1].toLowerCase()) > 0) {
                swap(a, begin, begin + 1);
            }
        } else {
            if (length == 2 && a[begin].compareTo(a[begin + 1]) > 0) {
                swap(a, begin, begin + 1);
            }
        }
    }

    private static void backsort3(String[] a, int x, int y, int z, boolean ignoreCase) {
        if (ignoreCase) {
            if (a[x].toLowerCase().compareTo(a[y].toLowerCase()) < 0) {
                if (a[x].toLowerCase().compareTo(a[z].toLowerCase()) < 0) {
                    if (a[y].toLowerCase().compareTo(a[z].toLowerCase()) < 0) {
                        backswap(a, x, z);
                    } else {
                        backswap3(a, x, y, z);
                    }
                } else {
                    backswap(a, x, y);
                }
            } else if (a[x].toLowerCase().compareTo(a[z].toLowerCase()) < 0) {
                backswap3(a, x, z, y);
            } else if (a[y].toLowerCase().compareTo(a[z].toLowerCase()) < 0) {
                backswap(a, y, z);
            }
        } else {
            if (a[x].compareTo(a[y]) < 0) {
                if (a[x].compareTo(a[z]) < 0) {
                    if (a[y].compareTo(a[z]) < 0) {
                        backswap(a, x, z);
                    } else {
                        backswap3(a, x, y, z);
                    }
                } else {
                    backswap(a, x, y);
                }
            } else if (a[x].compareTo(a[z]) < 0) {
                backswap3(a, x, z, y);
            } else if (a[y].compareTo(a[z]) < 0) {
                backswap(a, y, z);
            }
        }
    }

    private static void backswap(String[] a, int x, int y) {
        String t = a[x];
        a[x] = a[y];
        a[y] = t;
    }

    private static void backswap3(String[] a, int x, int y, int z) {
        String t = a[x];
        a[x] = a[y];
        a[y] = a[z];
        a[z] = t;
    }

    private static void sort3(String[] a, int x, int y, int z, boolean ignoreCase) {
        if (ignoreCase) {
            if (a[x].toLowerCase().compareTo(a[y].toLowerCase()) > 0) {
                if (a[x].toLowerCase().compareTo(a[z].toLowerCase()) > 0) {
                    if (a[y].toLowerCase().compareTo(a[z].toLowerCase()) > 0) {
                        swap(a, x, z);
                    } else {
                        swap3(a, x, y, z);
                    }
                } else {
                    swap(a, x, y);
                }
            } else if (a[x].toLowerCase().compareTo(a[z].toLowerCase()) > 0) {
                swap3(a, x, z, y);
            } else if (a[y].toLowerCase().compareTo(a[z].toLowerCase()) > 0) {
                swap(a, y, z);
            }
        } else {
            if (a[x].compareTo(a[y]) > 0) {
                if (a[x].compareTo(a[z]) > 0) {
                    if (a[y].compareTo(a[z]) > 0) {
                        swap(a, x, z);
                    } else {
                        swap3(a, x, y, z);
                    }
                } else {
                    swap(a, x, y);
                }
            } else if (a[x].compareTo(a[z]) > 0) {
                swap3(a, x, z, y);
            } else if (a[y].compareTo(a[z]) > 0) {
                swap(a, y, z);
            }
        }
    }

    private static void swap(String[] a, int x, int y) {
        String t = a[x];
        a[x] = a[y];
        a[y] = t;
    }

    private static void swap3(String[] a, int x, int y, int z) {
        String t = a[x];
        a[x] = a[y];
        a[y] = a[z];
        a[z] = t;
    }

    private static void backquickSort(String[] a, int begin, int length, boolean ignoreCase) {
        int i, j, leftLength, rightLength, t;
        String x;
        while (length >= 3) {
            t = length - 1;
            j = t + begin;
            i = (t >> 1) + begin;
            backsort3(a, begin, i, j, ignoreCase);
            if (length == 3) {
                return;
            }
            x = a[i];
            i = begin + 1;
            j--;
            do {
                if (ignoreCase) {
                    while (a[i].toLowerCase().compareTo(x.toLowerCase()) > 0) {
                        i++;
                    }
                    while (a[j].toLowerCase().compareTo(x.toLowerCase()) < 0) {
                        j--;
                    }
                } else {
                    while (a[i].compareTo(x) > 0) {
                        i++;
                    }
                    while (a[j].compareTo(x) < 0) {
                        j--;
                    }
                }
                if (i < j) {
                    backswap(a, i, j);
                } else {
                    if (i == j) {
                        i++;
                        j--;
                    }
                    break;
                }
            } while (++i <= --j);
            leftLength = (j - begin) + 1;
            rightLength = (begin - i) + length;
            if (leftLength < rightLength) {
                if (leftLength > 1) {
                    backquickSort(a, begin, leftLength, ignoreCase);
                }
                begin = i;
                length = rightLength;
            } else {
                if (rightLength > 1) {
                    backquickSort(a, i, rightLength, ignoreCase);
                }
                length = leftLength;
            }
        }
        if (ignoreCase) {
            if (length == 2 && a[begin].toLowerCase().compareTo(a[begin + 1].toLowerCase()) < 0) {
                backswap(a, begin, begin + 1);
            }
        } else {
            if (length == 2 && a[begin].compareTo(a[begin + 1]) < 0) {
                backswap(a, begin, begin + 1);
            }
        }
    }
}
