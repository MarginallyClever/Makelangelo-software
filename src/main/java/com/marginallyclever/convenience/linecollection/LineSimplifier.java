package com.marginallyclever.convenience.linecollection;

import org.jetbrains.annotations.NotNull;

public interface LineSimplifier {
    @NotNull LineCollection simplify(double distanceTolerance);
}
