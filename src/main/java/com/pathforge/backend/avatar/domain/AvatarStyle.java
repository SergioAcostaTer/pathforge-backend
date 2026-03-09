package com.pathforge.backend.avatar.domain;

public enum AvatarStyle {

    PIXAR(
            """
            transform this person into a Pixar movie 3D character,
            keep the exact face and identity of the person,
            upper body portrait shoulders and chest visible,
            large expressive cartoon eyes, smooth plastic-like skin,
            slightly enlarged head with Pixar proportions,
            dark navy blazer over white t-shirt, subtle confident smile,
            soft warm studio lighting, transparent background,
            ultra clean polished 3D render, toy figurine quality,
            Pixar animation studio style, professional avatar
            """,
            "photorealistic, hyperrealistic, small eyes, full body, harsh shadows, flat illustration, 2D, anime, distorted face, blurry, watermark, text"
    ),

    CARTOON(
            """
            transform this person into a 3D cartoon avatar,
            preserve the exact face and identity of the person,
            upper body portrait shoulders and chest visible,
            slightly cartoonish stylized features, mildly expressive eyes,
            smooth stylized skin, subtle cartoon proportions,
            dark navy blazer over white t-shirt, subtle confident smile,
            soft warm studio lighting, pure white background,
            high quality polished 3D render, professional app avatar
            """,
            "photorealistic, hyperrealistic, full body, overly exaggerated cartoon, extreme Pixar proportions, harsh shadows, flat illustration, 2D, anime, distorted face, blurry, watermark, text"
    ),

    CINEMATIC(
            """
            transform this person into a cinematic stylized 3D character,
            keep the exact face and identity of the person,
            upper body portrait shoulders and chest visible,
            semi-stylized realistic proportions, high-end game character quality,
            detailed facial features, smooth stylized skin with subtle artistic flair,
            dark navy blazer over white t-shirt, subtle confident expression,
            dramatic soft cinematic lighting, transparent background,
            AAA game character render quality, professional avatar
            """,
            "full cartoon, Pixar, toy figurine, flat illustration, 2D, anime, photorealistic, distorted face, blurry, full body, watermark, text"
    ),

    GTA5(
            """
            transform this person into a GTA V video game character,
            keep the face and identity of the person,
            upper body portrait shoulders and chest visible,
            hyper-detailed game character 3D model,
            slightly angular and defined facial features, sharp textures,
            urban streetwear outfit,
            dramatic cinematic lighting, dark urban atmosphere,
            Rockstar Games art style, gritty realistic game render
            """,
            "cartoon, anime, Pixar, flat illustration, 2D, blurry, distorted face, watermark, text"
    );

    private final String prompt;
    private final String negativePrompt;

    AvatarStyle(String prompt, String negativePrompt) {
        this.prompt = prompt;
        this.negativePrompt = negativePrompt;
    }

    public String prompt() { return prompt; }
    public String negativePrompt() { return negativePrompt; }
}
