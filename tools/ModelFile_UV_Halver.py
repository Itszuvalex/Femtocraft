
import sys, math

with open(sys.argv[2], 'w') as out_obj_file:
  with open(sys.argv[1], 'r') as in_obj_file:
      for line in in_obj_file:
          objectName = ""
          if line.startswith("# object"):
            parts = line.split(' ')
            objectName = parts[2]
            print("Processing object " + objectName)

          if line.startswith("vt"):
              parts = line.split(' ')
              u = float(parts[1])
              v = float(parts[2])
              w = float(parts[3])
              u = math.fabs(math.fmod(u, 1.))
              v = math.fabs(math.fmod(v, 1.))
              w = math.fabs(math.fmod(w, 1.))
              out_obj_file.write("vt " + "{:.4f}".format(u) + ' ' + "{:.4f}".format(v) + ' ' + "{:.4f}".format(w) + '\n')
          else:
              out_obj_file.write(line)
