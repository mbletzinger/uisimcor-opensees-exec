system BandGeneral
# system SparseGeneral
# system BandSPD                
 
# constraints Transformation    
constraints Penalty 1E20 1E20
numberer Plain                

#test EnergyIncr 1.0e-12  20 0 
test NormDispIncr 1.0e-5 10 0

#algorithm Newton              
algorithm ModifiedNewton

integrator LoadControl 1      
analysis Static               
		

