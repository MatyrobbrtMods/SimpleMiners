var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

function initializeCoreMod() {
    var ret = {};
    ret['datagenFix'] = {
        'target': {
            'type': 'METHOD',
            'class': 'it.zerono.mods.extremereactors.datagen.DataGenerationHandler',
            'methodName': 'gatherData',
            'methodDesc': '(Lnet/minecraftforge/data/event/GatherDataEvent;)V'
        },
        'transformer': function (node) {
            // Opcodes.RETURN
            node.instructions.insert(new InsnNode(177));
            return node;
        }
    }
    return ret;
}