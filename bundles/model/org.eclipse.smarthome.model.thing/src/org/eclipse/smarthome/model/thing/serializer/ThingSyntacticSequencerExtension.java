package org.eclipse.smarthome.model.thing.serializer;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynNavigable;

/**
 * 
 * @author Alex Tugarev
 *
 */
@SuppressWarnings("restriction") 
public class ThingSyntacticSequencerExtension extends ThingSyntacticSequencer {
    
    @Override
    protected void emit_ModelThing_ThingKeyword_0_q(EObject semanticObject,
            ISynNavigable transition, List<INode> nodes) {
        ILeafNode node = nodes != null && nodes.size() == 1 && nodes.get(0) instanceof ILeafNode ? (ILeafNode) nodes.get(0) : null;
        Keyword keyword = grammarAccess.getModelThingAccess().getThingKeyword_0();
        acceptUnassignedKeyword(keyword, keyword.getValue(), node);
    }

}
