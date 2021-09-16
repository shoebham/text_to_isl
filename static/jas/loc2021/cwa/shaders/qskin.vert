// qskin.vert
// Vertex shader. Does skinning, with morph adjustments, and arm-twisting.

#ifdef GL_ES
	precision highp float;
#endif

#define DO_TWIST ___DO_TWIST___
#define USE_TXTR ___USE_TXTR___

//========  Attributes.  ========

attribute vec3 BindPos;
attribute vec3 BindNorm;

attribute vec2 VSTexCoord0;
attribute vec4 BoneIxs;
attribute vec4 BoneWeights;
attribute vec4 BoneTwists;

attribute vec3 MorphPosA;
attribute vec3 MorphPosB;
attribute vec3 MorphPosC;
attribute vec3 MorphPosD;
attribute vec3 MorphNormA;
attribute vec3 MorphNormB;
attribute vec3 MorphNormC;
attribute vec3 MorphNormD;


//========  Uniforms.  ========

uniform mat4 ModelViewMat;
uniform mat4 ModelViewProjMat;

/*
TR transform data for each bone's inverse bind pose and its
current pose (both w.r.t. the global frame).  The composition of these
two transforms is the global transform from the bind pose to the current
pose.
(This data replaces the earlier matrix transforms)

So, conceptually we have four vectors:

   vec3 InverseBindPoseTranslation
   vec4 InverseBindPoseRotation
   vec3 CurrentPoseTranslation
   vec4 CurrentPoseRotation

but to overcome limitations in earlier GLES implementations, we pack
these four items into a single mat4 uniform.
*/

// NB The appropriate value must be substituted here before compilation:
//
const int N_BONES = ___N_BONES___;
#if USE_TXTR
uniform sampler2D SkelXforms;
uniform int SkelXformsWidth;
uniform int SkelXformsHeight;
mat4 getSkelXformsMatrix( const in float i ) {
	float j = i * 4.0;
	float x = mod( j, float( SkelXformsWidth ) );
	float y = floor( j / float( SkelXformsWidth ) );
	float dx = 1.0 / float( SkelXformsWidth );
	float dy = 1.0 / float( SkelXformsHeight );
	y = dy * ( y + 0.5 );
	vec4 v1 = texture2D( SkelXforms, vec2( dx * ( x + 0.5 ), y ) );
	vec4 v2 = texture2D( SkelXforms, vec2( dx * ( x + 1.5 ), y ) );
	vec4 v3 = texture2D( SkelXforms, vec2( dx * ( x + 2.5 ), y ) );
	vec4 v4 = texture2D( SkelXforms, vec2( dx * ( x + 3.5 ), y ) );
	mat4 bone = mat4( v1, v2, v3, v4 );
	return bone;
}
#else
uniform mat4 SkelXforms[N_BONES];
mat4 getSkelXformsMatrix( const in float i ) {
	mat4 bone = SkelXforms[ int(i) ];
	return bone;
}
#endif

#define InvBindPoseTrans( m4 )  ((m4[0]).xyz)
#define InvBindPoseRot( m4 )    (m4[1])
#define CurPoseTrans( m4 )      ((m4[2]).xyz)
#define CurPoseRot( m4 )        (m4[3])

#if DO_TWIST
// See notes at end of this file re shoulder- & wrist-twisting.
#if USE_TXTR
uniform sampler2D BoneTwistData;
uniform int BoneTwistWidth;
uniform int BoneTwistHeight;
vec4 getBoneTwistData( const in float i ) {
	float j = i * 1.0;
	float x = mod( j, float( BoneTwistWidth ) );
	float y = floor( j / float( BoneTwistWidth ) );
	float dx = 1.0 / float( BoneTwistWidth );
	float dy = 1.0 / float( BoneTwistHeight );
	y = dy * ( y + 0.5 );
	vec4 twist = texture2D( BoneTwistData, vec2( dx * ( x + 0.5 ), y ) );
	return twist;
}
#else
uniform vec4 BoneTwistData[N_BONES];
vec4 getBoneTwistData( const in float i ) {
	vec4 twist = BoneTwistData[ int(i) ];
	return twist;
}
#endif
#endif

// Weights for the four pairs of morph attributes.
uniform vec4 MorphWeights;


//========  Vertex shader outputs.  ========

varying vec3 Normal;
varying vec2 TexCoord0;


//========  Local workspace.  ========

vec3 morphedBindPos;
vec3 morphedBindNorm;

vec4 skinMatRow[3];


//========  Supporting function declarations.  ========

// Applies the given morph adjustment to morphedBindPos, morphedBindNorm.
//
void addMorph(float mWght, vec3 mPos, vec3 mNorm);
//-----------

// Adds the given bone position, weighted as specified, and incorporating
// the given twist scale factor, to each of the three skinning matrix
// rows, in skinMatRow.
//
void addBone(float bf, float bWght, float twistScale);
//----------

// Performs a SLerp operation on unit quaternions qa, qb with scale
// factor T, and sets qr to be the result.
void setSlerp(out vec4 qr, in vec4 qa, in vec4 qb, in float T);
//-----------

// Scales rotation quaternion q by the factor T, and sets qr to be
// the result.
void setScaleRot(out vec4 qr, in vec4 q, in float T);
//--------------

// Sets qr to be the product of the rotation quaternions qa and qb.
//
void setQProd(out vec4 qr, in vec4 qa, in vec4 qb);
//-----------

// Sets tr to be the result of applying to t the rotation defined by
// the quaternion q.
//
void setRotate(out vec3 tr, in vec3 t, in vec4 q);
//------------

// Sets the TR transform (tr,rr) to be the composition of the two
// transforms (ta,ra) and (tb,rb).
//
void setTRXProd(
//-------------
    out vec3 tr, out vec4 rr,
    in vec3 ta, in vec4 ra, in vec3 tb, in vec4 rb);

// Sets mr to be the TR matrix corresponding to the TR transform (t,r).
//
void setTRMat(out mat4 mr, vec3 t, vec4 r);
//-----------

// Sets smr to be the first three rows of the TR matrix corresponding
// to the TR transform (t,r).
//
void setTRMatRows(out vec4 smr[3], vec3 t, vec4 r);
//---------------

// Sets boneSkinMatRows to be the first three rows of the TR matrix
// corresponding to the TR transform uniforms for bone b.
//
void setSkinMatRowsForBone(out vec4 boneSkinMatRows[3], float bf);
//------------------------

#if DO_TWIST
// Sets boneSkinMatRows to be the first three rows of the TR matrix
// corresponding to the TR transform uniforms for bone b, with the
// twist adjustment defined by qtw.
//
void setSkinMatRowsForBoneWithTwist(
    out vec4 boneSkinMatRows[3], float bf, in vec4 qtw);
#endif

//================  main() for vertex shader.  ================

void main(void)
{
    // Pass texture coordinates straight through to the fragment
    // shader.
	TexCoord0 = VSTexCoord0;

    // Produce -- in morphedBindPos, morphedBindNorm -- a copy of the
    // bind pose, but adjusted to include the current morph settings,
    // if any.
    morphedBindPos = BindPos;  morphedBindNorm = BindNorm;

    float mWtA = MorphWeights.x, mWtB = MorphWeights.y,
          mWtC = MorphWeights.z, mWtD = MorphWeights.w;

    if (mWtA != 0.0) { addMorph(mWtA, MorphPosA, MorphNormA); }
    if (mWtB != 0.0) { addMorph(mWtB, MorphPosB, MorphNormB); }
    if (mWtC != 0.0) { addMorph(mWtC, MorphPosC, MorphNormC); }
    if (mWtD != 0.0) { addMorph(mWtD, MorphPosD, MorphNormD); }

    // Create the first three rows of the skinning matrix from this
    // vertex's influencing bones.
    skinMatRow[0] = skinMatRow[1] = skinMatRow[2] = vec4(0.0);

    float bWtX = BoneWeights.x, bWtY = BoneWeights.y,
          bWtZ = BoneWeights.z, bWtW = BoneWeights.w;

    // The first bone weight must be non-zero, but the others may or
    // may not be so.
    addBone(BoneIxs.x, bWtX, BoneTwists.x);
    if (bWtY != 0.0) { addBone(BoneIxs.y, bWtY, BoneTwists.y); }
    if (bWtZ != 0.0) { addBone(BoneIxs.z, bWtZ, BoneTwists.z); }
    if (bWtW != 0.0) { addBone(BoneIxs.w, bWtW, BoneTwists.w); }

	// Apply the skinning matrix to the morph-adjusted bind pose.
	vec4 mBP = vec4(morphedBindPos, 1.0), mBM = vec4(morphedBindNorm, 0.0);
	vec4 vtxPos = vec4(
	    dot(skinMatRow[0], mBP), dot(skinMatRow[1], mBP),
	    dot(skinMatRow[2], mBP), 1.0);
	vec4 vtxNorm = vec4(
	    dot(skinMatRow[0], mBM), dot(skinMatRow[1], mBM),
	    dot(skinMatRow[2], mBM), 0.0);

	// Apply the view transforms to produce the position and norm
	// values for the fragment shader.
	gl_Position = ModelViewProjMat * vtxPos;
	Normal = normalize((ModelViewMat * vtxNorm).xyz);
}


//========  Supporting function definitions.  ========

const int SHOULDER_TWIST_PF     = 2;
const int WRIST_TWIST_PF        = 3;

// Macros
// (These were functions originally, but older GLES implementations
// seem to have quite tight limits on the number of permitted functions,
// so that's the reason for defining them as macros.
//
// * Tests whether the flag represented by prime pf is present in FLAGS.
// * Returns the integer value represented by the high-order 16 bits of n.
//   (Really 15 bits, assuming n is non-negative.)
// * Returns the integer value represented by the low-order 16 bits of n.
//
#define hasFlag(FLAGS, pf)      ((pf) * ((FLAGS)/(pf)) == (FLAGS))
#define upper16(n)              ((n) / (256 * 256))
#define lower16(n)              ((n) - 256 * 256 * upper16(n))

void addMorph(float mWght, vec3 mPos, vec3 mNorm)
{
    morphedBindPos += mPos * mWght;
    morphedBindNorm += mNorm * mWght;
}

void addBone(float bf, float bWght, float twistScale)
{
    // First, we must generate bone b's skinning TR matrix from its
    // current global and inverse bind pose transforms, allowing for
    // the possibility of a twist rotation factor in that skinning
    // transform.
    vec4 boneSMRow[3];

#if DO_TWIST
    // Check for shoulder/wrist twist adjustment.
    // Negative scale value means "no twist for this bone".
    if (0.0 <= twistScale)
    {
        vec4 qTwist = vec4(0.0, 0.0, 0.0, 1.0);
        vec4 tDataB = getBoneTwistData( bf );
        int FLAGS = int(tDataB.w);
        if (hasFlag(FLAGS, SHOULDER_TWIST_PF))
        {
            qTwist = vec4(tDataB.x, 0.0, 0.0, tDataB.y);
        }
        else if (hasFlag(FLAGS, WRIST_TWIST_PF))
        {
            vec4 qFullTwist = vec4(tDataB.x, 0.0, 0.0, tDataB.y);
            setScaleRot(qTwist, qFullTwist, twistScale);
        }
        // else ... should never happen.

        setSkinMatRowsForBoneWithTwist(boneSMRow, bf, qTwist);
    }
    else
#endif
    {
        setSkinMatRowsForBone(boneSMRow, bf);
    }

    skinMatRow[0] += boneSMRow[0] * bWght;
    skinMatRow[1] += boneSMRow[1] * bWght;
    skinMatRow[2] += boneSMRow[2] * bWght;
}

void setSlerp(out vec4 qr, in vec4 qa, in vec4 qb, in float T)
{
    float cOm, sOm, sSqOm, om, sA, sB, T_COMP = 1.0 - T;
    vec4 qb_ = qb;

    cOm = dot(qa, qb);
    if (cOm < 0.0) { cOm = -cOm; qb_ = - qb; }

    if (1.0 - cOm < 1e-5)
    {
        // In this case we should renormalize the final result, but
        // since that result will not see extended further use, we
        // omit that step.
        sA = T_COMP;   sB = T;
    }
    else
    {
        sSqOm = 1.0 - cOm * cOm;    sOm = sSqOm / sqrt(sSqOm);
        om = atan(sOm, cOm);
        sA = sin(om * T_COMP) / sOm;    sB = sin(om * T) / sOm;
    }

    qr = (qa * sA) + (qb_ * sB);
}

void setScaleRot(out vec4 qr, in vec4 q, in float T)
{
    vec4 qid;
    if (1.0 <= T + 1E-5)  { qr = q; }
    else if (T <= 1E-5)  { qr = vec4(0.0, 0.0, 0.0, 1.0); }
    else  { qid = vec4(0.0, 0.0, 0.0, 1.0);  setSlerp(qr, qid, q, T); }
}

void setQProd(out vec4 qr, in vec4 qa, in vec4 qb)
{
    vec3 va = qa.xyz, vb = qb.xyz;
    float wa = qa.w, wb = qb.w;
    qr.w = (wa * wb) - dot(va, vb);
    qr.xyz = (wa * vb) + (wb * va) + cross(va, vb);
}

void setRotate(out vec3 tr, in vec3 t, in vec4 q)
{
    // Make this a bit more efficient later.
    vec4 qc = vec4(-q.xyz, q.w);
    vec4 qt = vec4(t, 0.0);
    vec4 qq, qr;
    setQProd(qq, qt, qc);    // qq = qt X q*
    setQProd(qr, q, qq);     // qr = q X qq = q X qt X q*
    tr = qr.xyz;
}

void setTRXProd(
    out vec3 tr, out vec4 rr,
    in vec3 ta, in vec4 ra, in vec3 tb, in vec4 rb)
{
    vec3 tba;
    setRotate(tba, tb, ra);  // tba = ra(tb)
    tr = ta + tba;           // tr = ta + ra(tb)
    setQProd(rr, ra, rb);    // rr = ra X rb
}

void setTRMat(out mat4 mr, vec3 t, vec4 r)
{
    float x = r.x, y = r.y, z = r.z, w = r.w;
    float x2 = x+x, y2 = y+y, z2 = z+z,
        wx2 = w*x2, wy2 = w*y2, wz2 = w*z2,
        xx2 = x*x2, xy2 = x*y2, xz2 = x*z2,
        yy2 = y*y2, yz2 = y*z2, zz2 = z*z2;
    mr[0] = vec4(1.0-yy2-zz2,     xy2+wz2,     xz2-wy2, 0.0);  // col 0
    mr[1] = vec4(    xy2-wz2, 1.0-xx2-zz2,     yz2+wx2, 0.0);  // col 1
    mr[2] = vec4(    xz2+wy2,     yz2-wx2, 1.0-xx2-yy2, 0.0);  // col 2
    mr[3] = vec4(t, 1.0);
}

void setTRMatRows(out vec4 smr[3], vec3 t, vec4 r)
{
    float x = r.x, y = r.y, z = r.z, w = r.w;
    float x2 = x+x, y2 = y+y, z2 = z+z,
        wx2 = w*x2, wy2 = w*y2, wz2 = w*z2,
        xx2 = x*x2, xy2 = x*y2, xz2 = x*z2,
        yy2 = y*y2, yz2 = y*z2, zz2 = z*z2;
    smr[0] = vec4(1.0-yy2-zz2,     xy2-wz2,     xz2+wy2, t.x);
    smr[1] = vec4(    xy2+wz2, 1.0-xx2-zz2,     yz2-wx2, t.y);
    smr[2] = vec4(    xz2-wy2,     yz2+wx2, 1.0-xx2-yy2, t.z);
}

void setSkinMatRowsForBone(out vec4 boneSkinMatRows[3], float bf)
{
    mat4 boneXform = getSkelXformsMatrix(bf);
    vec3 skinTrans;
    vec4 skinRot;
    setTRXProd(skinTrans, skinRot,
        CurPoseTrans(boneXform), CurPoseRot(boneXform),
        InvBindPoseTrans(boneXform), InvBindPoseRot(boneXform) );
    setTRMatRows(boneSkinMatRows, skinTrans, skinRot);
}

#if DO_TWIST
void setSkinMatRowsForBoneWithTwist(
    out vec4 boneSkinMatRows[3], float bf, in vec4 qtw)
{
    mat4 boneXform = getSkelXformsMatrix(bf);
    vec3 skinTrans;
    vec4 skinRot, curRotWithTwist;
    setQProd(curRotWithTwist, CurPoseRot(boneXform), qtw);
    setTRXProd(skinTrans, skinRot,
        CurPoseTrans(boneXform), curRotWithTwist,
        InvBindPoseTrans(boneXform), InvBindPoseRot(boneXform));
    setTRMatRows(boneSkinMatRows, skinTrans, skinRot);
}
#endif

/*
------------------------------------------------------------------------
Wrist/Shoulder Twisting

attribute   vec4    BoneTwists;
    Define the value v (for each of the 4 bones influencing this vertex):
        v = clamp( vtx.boneEndDist(b)/bone.getLength(), 0, 1 )
    Then for each bone the value here is:
    - shoulder-twisting bone:  1 - v
    - wrist-twisting bone:     v
    - any other bone:          -1  (indicates no twisting)

uniform     vec4    BoneTwistData[74];

Representing a set of flags:
Each flag f is assigned a unique prime pf (from 2 3 5 7 11 13 ...)
Flags int FF is 1 * pfa * pfb * ... for each flag fi that is set.
So, the test for presence of flag fi in FF is: (FF / pfi) * pfi == FF

vec4 tdb = BoneTwistData[b] contains the following data for bone b

    tdb.xy: x and w components of the product of the relevant bone's
            local rotation and its invInitXRot (the y and z components
            of this product are always 0).
            For the shoulder-twisting bones, both x and y will be negated.
            The "relevant bone" is:
            - for a shoulder-twisting bone: the bone itself;
            - for a wrist-twisting bone: the bone's first child.
            If it helps, these values could be set to (-1.0, -1.0) when
            b does not participate in twisting.
    tdb.z:  parentBoneIndex           [upper 16 bits]
            child[0] bone index       [lower 16 bits]
    (NB  In fact we don't need/use this.)
    tdb.w:  Flags:
            f0 ~ 2: bone is [LR]UPA   (shoulder-twist flag)
            f1 ~ 3: bone is [LR]LRA   (wrist-twist flag)

------------------------------------------------------------------------
*/

//-------- (the end)  --------
