package com.okey.demagicandroid;

import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;

public interface IOxfordSimilarityFinder {
    void onFoundSimilarFaces(SimilarPersistedFace[] foundFaces);
}
