import sys
import os
import io

# Forcer l'encodage UTF-8 pour éviter le crash des emojis sur Windows
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

import logging

# Désactiver les logs de TensorFlow pour plus de clarté
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

try:
    from deepface import DeepFace
except ImportError:
    print("Error: DeepFace not installed")
    sys.exit(1)

def verify_faces(img1_path, img2_path):
    try:
        # Utiliser le modèle VGG-Face (très précis et standard)
        # On peut aussi utiliser 'Facenet' ou 'OpenFace'
        result = DeepFace.verify(
            img1_path = img1_path, 
            img2_path = img2_path, 
            model_name = 'VGG-Face',
            enforce_detection = False,
            detector_backend = 'opencv'
        )
        
        if result["verified"]:
            print("MATCH")
        else:
            print("NO_MATCH")
            
    except Exception as e:
        print(f"ERROR: {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python face_reco.py <image1> <image2>")
        sys.exit(1)
        
    img1 = sys.argv[1]
    img2 = sys.argv[2]
    
    if not os.path.exists(img1) or not os.path.exists(img2):
        print("ERROR: Image files not found")
        sys.exit(1)
        
    verify_faces(img1, img2)
