/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal.text;

import java.util.Locale;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.voice.text.AbstractRuleBasedInterpreter;
import org.eclipse.smarthome.io.voice.text.Expression;

/**
 * A human language command interpretation service.
 *
 * @author Tilman Kamp - Initial contribution and API
 *
 */
public class StandardInterpreter extends AbstractRuleBasedInterpreter {

    @Override
    public void createRules() {

        /****************************** ENGLISH ******************************/

        Expression onOff = alt(cmd("on", OnOffType.ON), cmd("off", OnOffType.OFF));
        Expression turn = alt("turn", "switch");
        Expression put = alt("put", "bring");
        Expression the = opt("the");

        addRules(
            Locale.ENGLISH,

        /* OnOffType */

        itemRule(seq(turn, the), /* item */ onOff),

        itemRule(seq(turn, onOff) /* item */),

        /* OpenCloseType */

        itemRule(seq(cmd("open", OpenClosedType.OPEN), the) /* item */),

        itemRule(seq(cmd("close", OpenClosedType.CLOSED), the) /* item */),

        /* IncreaseDecreaseType */

        itemRule(seq(cmd(alt("dim", "decrease", "lower", "soften"),
            IncreaseDecreaseType.DECREASE), the) /* item */),

        itemRule(seq(cmd(alt("brighten", "increase", "harden", "enhance"),
            IncreaseDecreaseType.INCREASE), the) /* item */),

        /* UpDownType */

        itemRule(seq(put, the), /* item */ cmd("up", UpDownType.UP)),

        itemRule(seq(put, the), /* item */ cmd("down", UpDownType.DOWN)),

        /* NextPreviousType */

        itemRule("move", /* item */ seq(opt("to"),
            alt(cmd("next", NextPreviousType.NEXT), cmd("previous", NextPreviousType.PREVIOUS)))),

        /* PlayPauseType */

        itemRule(seq(cmd("play", PlayPauseType.PLAY), the) /* item */),

        itemRule(seq(cmd("pause", PlayPauseType.PAUSE), the) /* item */),

        /* RewindFastForwardType */

        itemRule(seq(cmd("rewind", RewindFastforwardType.REWIND), the) /* item */),

        itemRule(seq(cmd(seq(opt("fast"), "forward"), RewindFastforwardType.FASTFORWARD), the) /* item */),

        /* StopMoveType */

        itemRule(seq(cmd("stop", StopMoveType.STOP), the) /* item */),

        itemRule(seq(cmd(alt("start", "move", "continue"), StopMoveType.MOVE), the) /* item */),

        /* RefreshType */

        itemRule(seq(cmd("refresh", RefreshType.REFRESH), the) /* item */)

        );

        /****************************** GERMAN ******************************/

        Expression einAnAus = alt(cmd("ein", OnOffType.ON), cmd("an", OnOffType.ON), cmd("aus", OnOffType.OFF));
        Expression denDieDas = opt(alt("den", "die", "das"));

        addRules(
            Locale.GERMAN,
            itemRule(seq(alt("schalt", "schalte", "mach"), denDieDas), /* item */ einAnAus),
            itemRule(seq(cmd("öffne", OpenClosedType.OPEN), denDieDas) /* item */),
            itemRule(seq(cmd(alt("schließ", "schließe"), OpenClosedType.OPEN), denDieDas) /* item */));

    }

}
