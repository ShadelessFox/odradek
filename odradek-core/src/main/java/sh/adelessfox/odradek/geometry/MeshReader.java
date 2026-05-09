package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.math.Vector3;

import java.util.Map;

public final class MeshReader {
    private Accessor indices;
    private Accessor positions;
    private Accessor normals;
    private Accessor weights;
    private Accessor joints;

    public MeshReader indices(Accessor indices) {
        if (this.indices != null) {
            throw new IllegalStateException("indices already set");
        }
        this.indices = indices;
        return this;
    }

    public MeshReader positions(Accessor positions) {
        if (this.positions != null) {
            throw new IllegalStateException("positions already set");
        }
        this.positions = positions;
        return this;
    }

    public MeshReader normals(Accessor normals) {
        if (this.normals != null) {
            throw new IllegalStateException("normals already set");
        }
        this.normals = normals;
        return this;
    }

    public MeshReader weights(Accessor weights) {
        if (this.weights != null) {
            throw new IllegalStateException("weights already set");
        }
        this.weights = weights;
        return this;
    }

    public MeshReader joints(Accessor joints) {
        if (this.joints != null) {
            throw new IllegalStateException("joints already set");
        }
        this.joints = joints;
        return this;
    }

    public Mesh read() {
        if (indices == null) {
            throw new IllegalStateException("indices not set");
        }
        if (positions == null) {
            throw new IllegalStateException("positions not set");
        }
        Map<Semantic, Accessor> vertices = Map.of(
            Semantic.POSITION, positions,
            Semantic.NORMAL, normals,
            Semantic.WEIGHTS, weights,
            Semantic.JOINTS, joints
        );
        return new Mesh(indices, vertices, Vector3.ONE);
    }
}
