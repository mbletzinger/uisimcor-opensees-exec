pattern Plain ${StepNumber}	Linear {
${LoadPattern}
}
analyze 1
remove loadPattern ${StepNumber}
puts "\"Current step %d - done #:\"\n"