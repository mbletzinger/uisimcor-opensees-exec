# _____________________________________________________________________________
#
# Start of model generation
# _____________________________________________________________________________

# Create ModelBuilder (with three-dimensions and 6 DOF/node)
model BasicBuilder -ndm 2

# --------------------------------------------------------------------
#    Create nodes
# --------------------------------------------------------------------

#    tag        X         Y      
node  1  7.308       0
node  2  0.000  4.5675
node  3  7.308  4.5675
node  4 14.616  4.5675


# --------------------------------------------------------------------
#     Boundary condition
# --------------------------------------------------------------------
# Fix supports at base of columns
#    tag   DX   DY   RZ 
fix   1     1    1    0 
fix   2     0    1    0 
fix   4     0    1    0 


# --------------------------------------------------------------------
#     Material property
# --------------------------------------------------------------------
# Define materials for nonlinear columns
# STEEL
#                        tag   fy        E0       b
uniaxialMaterial Steel01  1    1E15  1.999E08     1


# Define wide-flange section: Column
# ----------------------------------------------------------
# secID - section ID number
set secID 1
# matID - material ID number 
set matID 1
# d  = nominal depth
set d 0.3683
# tw = web thickness
set tw 0.014986
# bf = flange width
set bf 0.37338
# tf = flange thickness
set tf 0.023876
# nfdw = number of fibers along web depth 
set nfdw 20
# nftw = number of fibers along web thickness
set nftw 1
# nfbf = number of fibers along flange width
set nfbf 10
# nftf = number of fibers along flange thickness
set nftf 1
  
source Wsection.tcl
Wsection $secID $matID $d $tw $bf $tf $nfdw $nftw $nfbf $nftf
# -----------------------------------------------------------

# Define wide-flange section: Beam
# ----------------------------------------------------------
# secID - section ID number
set secID 2
# matID - material ID number 
set matID 1
# d  = nominal depth
set d 0.25908
# tw = web thickness
set tw 0.010668
# bf = flange width
set bf 0.25654
# tf = flange thickness
set tf 0.017272
# nfdw = number of fibers along web depth 
set nfdw 20
# nftw = number of fibers along web thickness
set nftw 1
# nfbf = number of fibers along flange width
set nfbf 10
# nftf = number of fibers along flange thickness
set nftf 1
  
source Wsection.tcl
Wsection $secID $matID $d $tw $bf $tf $nfdw $nftw $nfbf $nftf
# -----------------------------------------------------------



# Define column element
# ----------------------
# Geometry of column elements (To define the orientation of column)
#                tag $vecxzX $vecxzY $vecxzZ 
geomTransf Linear 1  

# Number of integration points along length of element
set np 5

# Create the coulumns using Beam-column elements
#                           tag ndI ndJ nsecs secID transfTag
element nonlinearBeamColumn  1   1   3   $np    1       1 
element nonlinearBeamColumn  2   2   3   $np    2       1 
element nonlinearBeamColumn  3   3   4   $np    2       1 

# ------------------------------
# End of model generation
# ------------------------------

