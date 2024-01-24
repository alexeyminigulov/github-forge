package net.minilex.mocapmod.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MocapRecorder {
    public RecordThread recordThread;
    public List<MocapAction> eventsList = Collections
            .synchronizedList(new ArrayList<MocapAction>());
    public String fileName;
}
